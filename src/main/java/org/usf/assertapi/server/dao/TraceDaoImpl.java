package org.usf.assertapi.server.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.*;
import org.usf.assertapi.server.model.AssertionExecution;
import org.usf.assertapi.server.model.AssertionResult;
import org.usf.assertapi.server.model.ExecutionState;

import java.util.LinkedList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.sql.Types.*;
import static org.usf.assertapi.server.utils.DaoUtils.inArgs;
import static org.usf.assertapi.server.utils.DaoUtils.ofEpochMilli;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TraceDaoImpl implements TraceDao {

    private final ObjectMapper mapper;
    private final JdbcTemplate template;

    @Override
    public List<AssertionResult> select(long[] ids, List<String> status) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT O_REQ.ID_REQ, VA_NME, VA_DSC, VA_MTH, VA_URI, VA_HDR, VA_BDY,"
                + " E_ASR.DH_STB_STR, E_ASR.DH_STB_END, E_ASR.VA_STB_SIZ, E_ASR.VA_STB_STT, E_ASR.DH_LTS_STR, E_ASR.DH_LTS_END, E_ASR.VA_LTS_SIZ, E_ASR.VA_LTS_STT, E_ASR.VA_LTS_STT, E_ASR.VA_STP, E_ASR.VA_STT"
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
            var stableApiExecution = new ExecutionInfo(
                    rs.getTimestamp("DH_STB_STR").getTime(),
                    rs.getTimestamp("DH_STB_END").getTime(),
                    rs.getInt("VA_STB_SIZ"),
                    rs.getInt("VA_STB_STT")
            );
            var latestApiExecution = new ExecutionInfo(
                    rs.getTimestamp("DH_LTS_STR").getTime(),
                    rs.getTimestamp("DH_LTS_END").getTime(),
                    rs.getInt("VA_LTS_SIZ"),
                    rs.getInt("VA_LTS_STT")
            );
            var comparisonResult = new ComparisonResult(
                    stableApiExecution,
                    latestApiExecution,
                    rs.getString("VA_STT") != null ? ComparisonStatus.valueOf(rs.getString("VA_STT")) : null,
                    rs.getString("VA_STP") != null ? ComparisonStage.valueOf(rs.getString("VA_STP")) : null
            );
            return new AssertionResult(
                    rs.getLong("ID_REQ"),
                    rs.getString("VA_NME"),
                    rs.getString("VA_DSC"),
                    rs.getString("VA_MTH"),
                    rs.getString("VA_URI"),
                    comparisonResult
            );
        });
        log.info("{} requests", list.size());
        return list;
    }

    @Override
    public List<AssertionExecution> select(Long id) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT E_ASR_EXC.ID_EXC, VA_USR, VA_OS, VA_ADR, VA_JRE, VA_BRC, VA_APP, VA_STB_RLS, VA_LTS_RLS, VA_STT, RESULT, OK, SKIP, ERROR, FAIL" +
                " FROM E_ASR_EXC LEFT JOIN (SELECT ID_EXC, COUNT(ID_EXC) as RESULT, COUNT(CASE WHEN VA_STT = 'OK' then 1 ELSE NULL END) as OK, COUNT(CASE WHEN VA_STT = 'SKIP' then 1 ELSE NULL END) as SKIP, COUNT(CASE WHEN VA_STT = 'ERROR' then 1 ELSE NULL END) as ERROR, COUNT(CASE WHEN VA_STT = 'FAIL' then 1 ELSE NULL END) as FAIL FROM E_ASR GROUP BY ID_EXC)" +
                " as E_ASR ON E_ASR.ID_EXC = E_ASR_EXC.ID_EXC");
        if(id != null) {
            q.append(" WHERE E_ASR_EXC.ID_EXC = ? ");
            args.add(id);
        }
        q.append(" ORDER BY E_ASR_EXC.ID_EXC DESC");
        return template.query(q.toString(), args.toArray(), (rs, i)->
                new AssertionExecution(
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
                        rs.getInt("ERROR"),
                        rs.getInt("FAIL")
                )
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(long idAsr, Long idReq, @NonNull ComparisonResult res) {
        var q = "INSERT INTO E_ASR(ID_EXC, ID_REQ, DH_STB_STR, DH_STB_END,"
                + " VA_STB_SIZ, VA_STB_STT, DH_LTS_STR, DH_LTS_END, VA_LTS_SIZ, VA_LTS_STT,"
                + " VA_STT, VA_STP)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            ExecutionInfo stableApiExecution = res.getStableApiExecution();
            ExecutionInfo latestApiExecution = res.getLatestApiExecution();

            ps.setLong(1, idAsr);
            if(idReq == null) {
                ps.setNull(2, BIGINT);
            }
            else {
                ps.setLong(2, idReq);
            }
            if(stableApiExecution != null) {
                ps.setTimestamp(3, ofEpochMilli(stableApiExecution.getStart()));
                ps.setTimestamp(4, ofEpochMilli(stableApiExecution.getEnd()));
                ps.setInt(5, stableApiExecution.getSize());
                ps.setInt(6, stableApiExecution.getStatus());
            } else {
                ps.setNull(3, TIMESTAMP);
                ps.setNull(4, TIMESTAMP);
                ps.setNull(5, INTEGER);
                ps.setNull(6, INTEGER);
            }
            if(latestApiExecution != null) {
                ps.setTimestamp(7, ofEpochMilli(latestApiExecution.getEnd()));
                ps.setTimestamp(8, ofEpochMilli(latestApiExecution.getStart()));
                ps.setInt(9, latestApiExecution.getSize());
                ps.setInt(10, latestApiExecution.getStatus());
            } else {
                ps.setNull(7, TIMESTAMP);
                ps.setNull(8, TIMESTAMP);
                ps.setNull(9, INTEGER);
                ps.setNull(10, INTEGER);
            }

            ps.setString(11, res.getStatus().toString());
            ps.setString(12, res.getStep() == null ? null : res.getStep().toString());
        });
        log.info("assersion {} ==> {}", idAsr, res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long register(@NonNull String app, @NonNull String latestRelease, @NonNull String stableRelease, @NonNull RuntimeEnvironement ctx,@NonNull ExecutionState status) {

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
    public void updateStatus(long id, @NonNull ExecutionState status){
        String q = "UPDATE E_ASR_EXC SET VA_STT = ? WHERE ID_EXC = ? ";
        template.update(q, ps-> {
            ps.setString(1, status.name());
            ps.setLong(2, id);
        });
    }
}
