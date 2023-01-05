package org.usf.assertapi.server.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.CompareStatus;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.server.model.ApiTrace;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

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
        StringBuilder q = new StringBuilder("SELECT ASR_REQ.VA_REQ_STT, API_REQ.ID_REQ, API_REQ.VA_API_NME, API_REQ.VA_API_DSC, API_REQ.VA_API_MTH, API_REQ.VA_API_URI, API_REQ.VA_API_HDR, API_REQ.VA_API_BDY"
                + " FROM ASR_REQ INNER JOIN API_REQ ON ASR_REQ.ID_REQ = API_REQ.ID_REQ WHERE 1=1");
        if(ids != null) {
            q.append(" AND ASR_REQ.ID_ASR IN ").append(inArgs(ids.length));
            for (long id : ids) {
                args.add(id);
            }
        }
        if(status != null) {
            q.append(" AND VA_REQ_STT IN ").append(inArgs(status.size()));
            args.addAll(status);
        }
        q.append(" ORDER BY ASR_REQ.ID_ASR DESC");
        var list = template.query(q.toString(), args.toArray(), (rs, i)-> {
            try {
                return new ApiTrace(
                        rs.getLong("ID_REQ"),
                        rs.getString("VA_API_NME"),
                        rs.getString("VA_API_DSC"),
                        rs.getString("VA_API_MTH"),
                        rs.getString("VA_API_URI"),
                        mapper.readValue(rs.getString("VA_API_HDR"), new TypeReference<Map<String, String>>(){}),
                        rs.getString("VA_API_BDY"),
                        rs.getString("VA_REQ_STT") != null ? CompareStatus.valueOf(rs.getString("VA_REQ_STT")) : null
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
        var q = "INSERT INTO ASR_REQ(ID_ASR, ID_REQ, VA_EXT_HST, VA_ACT_HST,"
                + " DH_EXT_STR, DH_EXT_END, DH_ACT_STR, DH_ACT_END,"
                + " VA_REQ_STT, VA_REQ_STP)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?)";
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
            ps.setTimestamp(5, ofEpochMilli(res.getLatestApiExecution().getStart()));
            ps.setTimestamp(6, ofEpochMilli(res.getLatestApiExecution().getEnd()));
            ps.setString(7, res.getStatus().toString());
            ps.setString(8, res.getStep() == null ? null : res.getStep().toString());
        });
        log.info("assersion {} ==> {}", idAsr, res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long register(String app, String stableRelease, String latestRelease, @NonNull RuntimeEnvironement ctx, TraceGroupStatus status) {

        var id = currentTimeMillis();
        var q = "INSERT INTO ASR_GRP(ID_ASR, VA_HST_USR, VA_HST_OS, VA_HST_ADR, VA_API_APP, VA_EXT_ENV, VA_ACT_ENV, VA_GRP_STT) VALUES(?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            ps.setLong(1, id);
            ps.setString(2, ctx.getUser());
            ps.setString(3, ctx.getOs());
            ps.setString(4, ctx.getAddress());
            ps.setString(5, app);
            ps.setString(6, latestRelease);
            ps.setString(7, stableRelease);
            ps.setString(8, status.name());
        });
        log.info("registered {} ==> {}", ctx, id);
        return id;
    }

    @Override
    public List<ApiTraceGroup> selectTraceGroup(Long id) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT ASR_GRP.ID_ASR, ASR_GRP.VA_HST_USR, ASR_GRP.VA_HST_OS, ASR_GRP.VA_HST_ADR, ASR_GRP.VA_API_APP, ASR_GRP.VA_EXT_ENV, ASR_GRP.VA_ACT_ENV, ASR_GRP.VA_GRP_STT, ASR_REQ.RESULT, ASR_REQ.OK, ASR_REQ.SKIP, ASR_REQ.KO" +
                " FROM ASR_GRP LEFT JOIN (SELECT ID_ASR, COUNT(ID_ASR) as RESULT, COUNT(CASE WHEN VA_REQ_STT = 'OK' then 1 ELSE NULL END) as OK, COUNT(CASE WHEN VA_REQ_STT = 'SKIP' then 1 ELSE NULL END) as SKIP, COUNT(CASE WHEN VA_REQ_STT = 'KO' then 1 WHEN VA_REQ_STT = 'FAIL' then 1 ELSE NULL END) as KO FROM ASR_REQ GROUP BY ID_ASR)" +
                " as ASR_REQ ON ASR_REQ.ID_ASR = ASR_GRP.ID_ASR");
        if(id != null) {
            q.append(" WHERE ASR_GRP.ID_ASR = ? ");
            args.add(id);
        }
        q.append(" ORDER BY ASR_GRP.ID_ASR DESC");
        return template.query(q.toString(), args.toArray(), (rs, i)->
            new ApiTraceGroup(
                    rs.getLong("ID_ASR"),
                    rs.getString("VA_HST_USR"),
                    rs.getString("VA_HST_OS"),
                    rs.getString("VA_HST_ADR"),
                    rs.getString("VA_API_APP"),
                    rs.getString("VA_EXT_ENV"),
                    rs.getString("VA_ACT_ENV"),
                    rs.getString("VA_GRP_STT") != null ? TraceGroupStatus.valueOf(rs.getString("VA_GRP_STT")) : null,
                    rs.getInt("RESULT"),
                    rs.getInt("SKIP"),
                    rs.getInt("OK"),
                    rs.getInt("KO")
            )
        );
    }

    @Override
    public void updateStatus(long id, TraceGroupStatus status){
        String q = "UPDATE ASR_GRP SET VA_GRP_STT = ? WHERE ID_ASR = ? ";
        template.update(q, ps-> {
            ps.setString(1, status.name());
            ps.setLong(2, id);
        });
    }
}
