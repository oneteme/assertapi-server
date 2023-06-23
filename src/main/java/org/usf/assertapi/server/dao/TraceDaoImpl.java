package org.usf.assertapi.server.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.ComparisonStatus;
import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.server.model.ApiTrace;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.ExecutionState;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static java.sql.Types.BIGINT;
import static org.usf.assertapi.server.utils.DaoUtils.inArgs;
import static org.usf.assertapi.server.utils.DaoUtils.ofEpochMilli;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TraceDaoImpl implements TraceDao {

    private final ObjectMapper mapper;
    private final JdbcTemplate template;

    @Override
    public List<ApiTrace> select(long[] ids, List<String> status) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT O_REQ.ID_REQ, VA_NME, VA_DSC, VA_MTH, VA_URI, VA_HDR, VA_BDY, E_ASR.VA_STT,"
                + " FROM E_ASR INNER JOIN O_REQ ON E_ASR.ID_REQ = O_REQ.ID_REQ WHERE 1=1");
        if(ids != null) {
            q.append(" AND E_ASR.ID_EXC IN ").append(inArgs(ids.length));
            for (long id : ids) {
                args.add(id);
            }
        }
        if(status != null) {
            q.append(" AND E_ASR.VA_STT IN ").append(inArgs(status.size()));
            args.addAll(status);
        }
        q.append(" ORDER BY E_ASR.ID_EXC DESC");
        var list = template.query(q.toString(), args.toArray(), (rs, i)-> {
            try {
                return new ApiTrace(
                        rs.getLong("ID_REQ"),
                        rs.getString("VA_NME"),
                        rs.getString("VA_DSC"),
                        rs.getString("VA_MTH"),
                        rs.getString("VA_URI"),
                        mapper.readValue(rs.getString("VA_HDR"), new TypeReference<Map<String, List<String>>>(){}),
                        rs.getString("VA_BDY"),
                        rs.getString("VA_STT") != null ? ComparisonStatus.valueOf(rs.getString("VA_STT")) : null
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("{} requests", list.size());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(long idAsr, Long idReq, @NonNull ComparisonResult res) {
        var q = "INSERT INTO E_ASR(ID_EXC, ID_REQ, DH_STB_STR, DH_STB_END,"
                + " VA_STB_SIZ, VA_STB_STT, DH_LTS_STR, DH_LTS_END, VA_LTS_SIZ, VA_LTS_STT,"
                + " VA_STT, VA_STP)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            ps.setLong(1, idAsr);
            if(idReq == null) {
                ps.setNull(2, BIGINT);
            }
            else {
                ps.setLong(2, idReq);
            }
            ps.setTimestamp(3, ofEpochMilli(res.getStableApiExecution().getStart()));
            ps.setTimestamp(4, ofEpochMilli(res.getStableApiExecution().getEnd()));
            ps.setInt(5, res.getStableApiExecution().getSize());
            ps.setInt(6, res.getStableApiExecution().getStatus());
            ps.setTimestamp(7, ofEpochMilli(res.getLatestApiExecution().getEnd()));
            ps.setTimestamp(8, ofEpochMilli(res.getLatestApiExecution().getStart()));
            ps.setInt(9, res.getLatestApiExecution().getSize());
            ps.setInt(10, res.getLatestApiExecution().getStatus());
            ps.setString(11, res.getStatus().toString());
            ps.setString(12, res.getStep() == null ? null : res.getStep().toString());
        });
        log.info("assersion {} ==> {}", idAsr, res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long register(@NonNull String app, @NonNull String stableRelease,@NonNull String latestRelease, @NonNull RuntimeEnvironement ctx,@NonNull ExecutionState status) {

        var id = currentTimeMillis();
        var q = "INSERT INTO E_ASR_EXC(ID_EXC, VA_USR, VA_OS, VA_ADR, VA_JRE, VA_BRC, VA_APP, VA_STB_RLS, VA_LTS_RLS, VA_STT) VALUES(?,?,?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            ps.setLong(1, id);
            ps.setString(2, ctx.getUser());
            ps.setString(3, ctx.getOs());
            ps.setString(4, ctx.getAddress());
            ps.setString(5, ctx.getJre());
            ps.setString(6, ctx.getBranch());
            ps.setString(7, app);
            ps.setString(8, stableRelease);
            ps.setString(9, latestRelease);
            ps.setString(10, status.name());
        });
        log.info("registered {} ==> {}", ctx, id);
        return id;
    }

    @Override
    public List<ApiTraceGroup> selectTraceGroup(Long id) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT E_ASR_EXC.ID_EXC, VA_USR, VA_OS, VA_ADR, VA_JRE, VA_BRC, VA_APP, VA_STB_RLS, VA_LTS_RLS, VA_STT, RESULT, OK, SKIP, KO" +
                " FROM E_ASR_EXC LEFT JOIN (SELECT ID_EXC, COUNT(ID_EXC) as RESULT, COUNT(CASE WHEN VA_STT = 'OK' then 1 ELSE NULL END) as OK, COUNT(CASE WHEN VA_STT = 'SKIP' then 1 ELSE NULL END) as SKIP, COUNT(CASE WHEN VA_STT = 'KO' then 1 WHEN VA_STT = 'FAIL' then 1 ELSE NULL END) as KO FROM E_ASR GROUP BY ID_EXC)" +
                " as E_ASR ON E_ASR.ID_EXC = E_ASR_EXC.ID_EXC");
        if(id != null) {
            q.append(" WHERE E_ASR_EXC.ID_EXC = ? ");
            args.add(id);
        }
        q.append(" ORDER BY E_ASR_EXC.ID_EXC DESC");
        return template.query(q.toString(), args.toArray(), (rs, i)->
            new ApiTraceGroup(
                    rs.getLong("ID_EXC"),
                    rs.getString("VA_USR"),
                    rs.getString("VA_OS"),
                    rs.getString("VA_ADR"),
                    rs.getString("VA_JRE"),
                    rs.getString("VA_BRC"),
                    rs.getString("VA_APP"),
                    rs.getString("VA_STB_RLS"),
                    rs.getString("VA_LTS_RLS"),
                    rs.getString("VA_STT") != null ? ExecutionState.valueOf(rs.getString("VA_STT")) : null,
                    rs.getInt("RESULT"),
                    rs.getInt("SKIP"),
                    rs.getInt("OK"),
                    rs.getInt("KO")
            )
        );
    }

    @Override
    public void updateStatus(long id, @NonNull ExecutionState status){
        String q = "UPDATE E_ASR_EXC SET VA_STT = ? WHERE ID_EXC = ? ";
        template.update(q, ps-> {
            ps.setString(1, status.name());
            ps.setLong(2, id);
        });
    }
}
