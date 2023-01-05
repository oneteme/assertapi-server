package org.usf.assertapi.server.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.ExecutionConfig;

import java.io.IOException;
import java.util.*;

import static org.usf.assertapi.server.utils.DaoUtils.inArgs;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RequestDaoImpl implements RequestDao {

    private final ObjectMapper mapper;
    private final JdbcTemplate template;

    @Override
    public List<ApiRequest> selectRequest(int[] ids, String app, List<String> envs) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT API_REQ.ID_REQ, VA_API_URI, VA_API_MTH, VA_API_HDR, VA_API_BDY,"
                + " VA_API_NME, VA_API_DSC, VA_API_VRS, VA_ASR_PRL, VA_ASR_ENB, VA_ASR_EXL"
                + " FROM API_REQ"
                + " INNER JOIN (SELECT ID_REQ, VA_API_ENV, VA_API_APP, COUNT(ID_REQ) as nb FROM API_REQ_GRP GROUP BY ID_REQ, VA_API_ENV, VA_API_APP HAVING nb >= ")
                .append(!CollectionUtils.isEmpty(envs) ? envs.size() : 0)
                .append(" ) as GRP ON GRP.ID_REQ = API_REQ.ID_REQ WHERE 1 = 1");

        if(ids != null) {
            q.append(" AND API_REQ.ID_REQ IN ").append(inArgs(ids.length));
            for (int id : ids) {
                args.add(id);
            }
        }

        if(app != null) {
            q.append(" AND VA_API_APP = ?");
            args.add(app);
        }

        if(!CollectionUtils.isEmpty(envs)) {
            q.append(" AND VA_API_ENV IN ").append(inArgs(envs.size()));
            args.addAll(envs);
        }

        q.append(" ORDER BY API_REQ.VA_API_NME ASC");

        var list = template.query(q.toString(), args.toArray(), (rs, i) -> {
            ApiRequest request = null;
            try {
                var conf = new ExecutionConfig(
                        rs.getBoolean("VA_ASR_ENB"),
                        rs.getBoolean("VA_ASR_PRL")
                );
                request = new ApiRequest(
                        rs.getLong("ID_REQ"),
                        rs.getString("VA_API_NME"),
                        rs.getInt("VA_API_VRS"),
                        rs.getString("VA_API_DSC"),
                        rs.getString("VA_API_URI"),
                        rs.getString("VA_API_MTH"),
                        mapper.readValue(rs.getString("VA_API_HDR"), new TypeReference<Map<String, String>>(){}),
                        rs.getString("VA_API_BDY"),
                        null,//TODO add acceptableStatus column
                        conf,
                        null,// response config => json column
                        null // stable reference
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return request;
        });
        log.info("app={}, env={} ==> {} requests", app, envs, list.size());
        return list;
    }

    @Override
    public void insertRequest(@NonNull long id, @NonNull ApiRequest req) {

        var q = "INSERT INTO API_REQ(ID_REQ, VA_API_URI, VA_API_MTH, VA_API_HDR, VA_API_BDY, "
                + "VA_API_NME, VA_API_DSC, "
                + "VA_ASR_PRL, VA_ASR_ENB, VA_ASR_EXL) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            try {
                ps.setLong(1, id);
                ps.setString(2, req.getUri());
                ps.setString(3, req.getMethod());
                ps.setString(4, mapper.writeValueAsString(req.getHeaders()));
                ps.setString(5, req.getBody());
                ps.setString(6, req.getName());
                ps.setString(7, req.getDescription());
                ps.setBoolean(8, req.getExecutionConfig().isParallel());
                ps.setBoolean(9, req.getExecutionConfig().isEnabled());
                ps.setString(10, mapper.writeValueAsString("[]"));  //TODO remove this column
                //TODO add respConfig json column 
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("request added : {}", req);
    }

    @Override
    public void updateRequest(@NonNull ApiRequest req) {

        var q = "UPDATE API_REQ SET VA_API_URI = ?, VA_API_MTH = ?, VA_API_BDY = ?, " +
                "VA_API_NME = ?, VA_API_DSC = ?, VA_API_HDR = ? " +
                "WHERE ID_REQ = ?";
        template.update(q, ps-> {
            try {
                ps.setString(1, req.getUri());
                ps.setString(2, req.getMethod());
                ps.setString(3, req.getBody());
                ps.setString(4, req.getName());
                ps.setString(5, req.getDescription());
                ps.setString(6, mapper.writeValueAsString(req.getHeaders()));
                ps.setLong(7, req.getId());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("request updated : {}", req);
    }

    @Override
    public void deleteRequest(@NonNull int[] ids){
        String q = "DELETE FROM API_REQ WHERE ID_REQ IN" + inArgs(ids.length);
        template.update(q, ps-> {
            for(var i=0; i<ids.length; i++) {
                ps.setInt(i+1, ids[i]);
            }
        });
        log.info("");
    }

    @Override
    public void insertRequestGroup(@NonNull long id, @NonNull String app, @NonNull List<String> envs) {
        var q = "INSERT INTO API_REQ_GRP(ID_REQ, VA_API_APP, VA_API_ENV)"
                + " VALUES(?,?,?)";
        template.batchUpdate(q, envs, envs.size(), (ps, r) -> {
            ps.setLong(1, id);
            ps.setString(2, app);
            ps.setString(3, r);
        });
        log.info("requestGroup added : app={}, envs={}", app, envs);
    }

    @Override
    public void deleteRequestGroup(@NonNull long id) {
        String q = "DELETE FROM API_REQ_GRP WHERE ID_REQ = ?";
        template.update(q, ps-> ps.setLong(1, id));
        log.info("");
    }

    @Override
    public void updateState(@NonNull int[] ids, boolean state) {

        String q = "UPDATE API_REQ SET VA_ASR_ENB = ? WHERE ID_REQ IN" + inArgs(ids.length);
        template.update(q, ps-> {
            ps.setBoolean(1, state);
            for(var i=0; i<ids.length; i++) {
                ps.setInt(i+2, ids[i]);
            }
        });
    }

    @Override
    public Long nextId(String col, String table) {
        return template.query("SELECT MAX(" + col + ") FROM " + table,
                rs-> rs.next() ? rs.getLong(1) : 0) + 1;
    }
}
