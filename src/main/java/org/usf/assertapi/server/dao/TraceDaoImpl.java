package org.usf.assertapi.server.dao;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.*;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
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
    public List<ApiAssertionsResultServer> select(long[] ids) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT ASR_REQ.ID_ASR, ASR_REQ.VA_EXT_HST, ASR_REQ.VA_ACT_HST, ASR_REQ.DH_EXT_STR, ASR_REQ.DH_EXT_END, ASR_REQ.DH_ACT_STR,"
                + " ASR_REQ.DH_ACT_END, ASR_REQ.VA_EXT_STT, ASR_REQ.VA_ACT_STT, ASR_REQ.VA_EXT_MIM, ASR_REQ.VA_ACT_MIM, ASR_REQ.VA_EXT_RES, VA_ACT_RES, ASR_REQ.VA_REQ_STT, ASR_REQ.VA_REQ_STP, API_REQ.ID_REQ, API_REQ.VA_API_URI, API_REQ.VA_API_MTH, API_REQ.VA_API_NME, API_REQ.VA_API_DSC"
                + " FROM ASR_REQ INNER JOIN API_REQ ON ASR_REQ.ID_REQ = API_REQ.ID_REQ WHERE 1=1");
        if(ids != null) {
            q.append(" AND ASR_REQ.ID_ASR IN ").append(inArgs(ids.length));
            for (long id : ids) {
                args.add(id);
            }
        }
        q.append(" ORDER BY ASR_REQ.ID_ASR DESC");
        var list = template.query(q.toString(), args.toArray(), (rs, i)-> {
            var expConf = new ApiExecution(
                    rs.getString("VA_EXT_HST"),
                    rs.getDate("DH_EXT_STR").getTime(),
                    rs.getDate("DH_EXT_END").getTime(),
                    rs.getInt("VA_EXT_STT"),
                    rs.getString("VA_EXT_MIM"),
                    rs.getBytes("VA_EXT_RES") != null ? new String(rs.getBytes("VA_EXT_RES")) : null
            );
            var actConf = new ApiExecution(
                    rs.getString("VA_ACT_HST"),
                    rs.getDate("DH_ACT_STR").getTime(),
                    rs.getDate("DH_ACT_END").getTime(),
                    rs.getInt("VA_ACT_STT"),
                    rs.getString("VA_ACT_MIM"),
                    rs.getBytes("VA_ACT_RES") != null ? new String(rs.getBytes("VA_ACT_RES")) : null
            );
            var res = new ApiAssertionsResult(
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
                    null,
                    rs.getString("VA_API_NME"),
                    rs.getString("VA_API_DSC"),
                    null
            );
            return new ApiAssertionsResultServer(
                    res,
                    req
            );
        });
        log.info("{} requests", list.size());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(long id, @NonNull ApiAssertionsResult res) {
        var q = "INSERT INTO ASR_REQ(ID_ASR, ID_REQ, VA_EXT_HST, VA_ACT_HST,"
                + " DH_EXT_STR, DH_EXT_END, DH_ACT_STR, DH_ACT_END, VA_EXT_STT, VA_ACT_STT,"
                + " VA_EXT_MIM, VA_ACT_MIM, VA_EXT_RES, VA_ACT_RES, VA_REQ_STT, VA_REQ_STP)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            ps.setLong(1, id);
            if(res.getId() == null) {
                ps.setNull(2, BIGINT);
            }
            else {
                ps.setLong(2, res.getId());
            }
            ps.setString(3, res.getExpExecution().getHost());
            ps.setString(4, res.getActExecution().getHost());
            ps.setTimestamp(5, ofEpochMilli(res.getExpExecution().getStart()));
            ps.setTimestamp(6, ofEpochMilli(res.getExpExecution().getEnd()));
            ps.setTimestamp(7, ofEpochMilli(res.getActExecution().getStart()));
            ps.setTimestamp(8, ofEpochMilli(res.getActExecution().getEnd()));
            ps.setInt(9, res.getExpExecution().getStatusCode());
            ps.setInt(10, res.getActExecution().getStatusCode());
            ps.setString(11, res.getExpExecution().getContentType());
            ps.setString(12, res.getActExecution().getContentType());
            ps.setBytes(13, res.getExpExecution().getResponse() == null ? null : res.getExpExecution().getResponse().getBytes());
            ps.setBytes(14, res.getActExecution().getResponse() == null ? null : res.getActExecution().getResponse().getBytes());
            ps.setString(15, res.getStatus().toString());
            ps.setString(16, res.getStep() == null ? null : res.getStep().toString());
        });
        log.info("assersion {} ==> {}", id, res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long register(@NonNull AssertionContext ctx) {

        var id = currentTimeMillis();
        var q = "INSERT INTO ASR_GRP(ID_ASR, VA_HST_USR, VA_HST_OS, VA_HST_ADR) VALUES(?,?,?,?)";
        template.update(q, ps-> {
            ps.setLong(1, id);
            ps.setString(2, ctx.getUser());
            ps.setString(3, ctx.getOs());
            ps.setString(4, ctx.getAddress());
        });
        log.info("registered {} ==> {}", ctx, id);
        return id;
    }
}
