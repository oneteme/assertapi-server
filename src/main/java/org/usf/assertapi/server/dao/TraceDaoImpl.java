package org.usf.assertapi.server.dao;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.*;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import java.util.LinkedList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.sql.Types.BIGINT;
import static org.usf.assertapi.server.utils.DaoUtils.inArgs;
import static org.usf.assertapi.server.utils.DaoUtils.ofEpochMilli;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TraceDaoImpl implements TraceDao {

    private final JdbcTemplate template;

    @Override
    public List<AssertionResultServer> select(long[] ids, List<String> status) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT ASR_REQ.ID_ASR, ASR_REQ.VA_EXT_HST, ASR_REQ.VA_ACT_HST, ASR_REQ.DH_EXT_STR, ASR_REQ.DH_EXT_END, ASR_REQ.DH_ACT_STR,"
                + " ASR_REQ.DH_ACT_END, ASR_REQ.VA_REQ_STT, ASR_REQ.VA_REQ_STP, API_REQ.ID_REQ, API_REQ.VA_API_URI, API_REQ.VA_API_MTH, API_REQ.VA_API_NME, API_REQ.VA_API_DSC"
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
            var expConf = new ExecutionInfo(
//                    rs.getString("VA_EXT_HST"),
                    rs.getDate("DH_EXT_STR").getTime(),
                    rs.getDate("DH_EXT_END").getTime(),
                    0 //TODO add column size
            );
            var actConf = new ExecutionInfo(
//                    rs.getString("VA_ACT_HST"),
                    rs.getDate("DH_ACT_STR").getTime(),
                    rs.getDate("DH_ACT_END").getTime(),
                    0 //TODO add column size
            );
            var res = new AssertionResult(
                    rs.getLong("ID_ASR"),
                    expConf,
                    actConf,
                    rs.getString("VA_REQ_STT") != null ? TestStatus.valueOf(rs.getString("VA_REQ_STT")) : null,
                    rs.getString("VA_REQ_STP") != null ? TestStep.valueOf(rs.getString("VA_REQ_STP")) : null
            );
            var req = new ApiRequest(
                    rs.getLong("ID_REQ"),
                    rs.getString("VA_API_URI"),
                    rs.getString("VA_API_MTH"),
                    null,
                    rs.getString("VA_API_NME"),
                    rs.getString("VA_API_DSC"),
                    (short)200,
                    null,//TODO
                    null //TODO
            );
            return new AssertionResultServer(
                    res,
                    req
            );
        });
        log.info("{} requests", list.size());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(long id, @NonNull AssertionResult res) {
        var q = "INSERT INTO ASR_REQ(ID_ASR, ID_REQ, VA_EXT_HST, VA_ACT_HST,"
                + " DH_EXT_STR, DH_EXT_END, DH_ACT_STR, DH_ACT_END,"
                + " VA_REQ_STT, VA_REQ_STP)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            ps.setLong(1, id);
            if(res.getId() == null) {
                ps.setNull(2, BIGINT);
            }
            else {
                ps.setLong(2, res.getId());
            }
//            ps.setString(3, res.getExpExecution().getHost());
//            ps.setString(4, res.getActExecution().getHost());
            ps.setTimestamp(5, ofEpochMilli(res.getExpExecution().getStart()));
            ps.setTimestamp(6, ofEpochMilli(res.getExpExecution().getEnd()));
            ps.setTimestamp(7, ofEpochMilli(res.getActExecution().getStart()));
            ps.setTimestamp(8, ofEpochMilli(res.getActExecution().getEnd()));
            ps.setString(9, res.getStatus().toString());
            ps.setString(10, res.getStep() == null ? null : res.getStep().toString());
        });
        log.info("assersion {} ==> {}", id, res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long register(@NonNull AssertionEnvironement ctx, String app, String latestRelease, String stableRelease, TraceGroupStatus status) {

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
    public void updateStatus(@NonNull long id, TraceGroupStatus status){
        String q = "UPDATE ASR_GRP SET VA_GRP_STT = ? WHERE ID_ASR = ? ";
        template.update(q, ps-> {
            ps.setString(1, status.name());
            ps.setLong(2, id);
        });
    }
}
