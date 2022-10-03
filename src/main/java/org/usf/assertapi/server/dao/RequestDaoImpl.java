package org.usf.assertapi.server.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.AssertionConfig;
import org.usf.assertapi.server.model.ApiRequestServer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.usf.assertapi.server.utils.DaoUtils.inArgs;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RequestDaoImpl implements RequestDao {

    private final ObjectMapper mapper;
    private final JdbcTemplate template;

    @Override
    public List<ApiRequestServer> select(int[] ids, String app, String env) {

        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT ID_REQ, VA_API_URI, VA_API_MTH, VA_API_HDR, VA_API_BDY, VA_API_CHR, "
                + "VA_API_NME, VA_API_DSC, VA_API_APP, VA_API_ENV, VA_ASR_PRL, VA_ASR_STR, VA_ASR_ENB, VA_ASR_DBG, VA_ASR_EXL "
                + "FROM API_REQ WHERE 1 = 1");
        if(app != null) {
            q.append(" AND VA_API_APP = ?");
            args.add(app);
        }
        if(env != null) {
            q.append(" AND VA_API_ENV = ?");
            args.add(env);
        }
        if(ids != null) {
            q.append(" AND ID_REQ IN ").append(inArgs(ids.length));
            for (int id : ids) {
                args.add(id);
            }
        }

        var list = template.query(q.toString(), args.toArray(), (rs, i)-> {
            ApiRequestServer req;
            try {
                var conf = new AssertionConfig(
                        rs.getBoolean("VA_ASR_DBG"),
                        rs.getBoolean("VA_ASR_ENB"),
                        rs.getBoolean("VA_ASR_STR"),
                        rs.getBoolean("VA_ASR_PRL"),
                        mapper.readValue(rs.getString("VA_ASR_EXL"), String[].class));
                var apiReq = new ApiRequest(
                        rs.getLong("ID_REQ"),
                        rs.getString("VA_API_URI"),
                        rs.getString("VA_API_MTH"),
                        mapper.readValue(rs.getString("VA_API_HDR"), new TypeReference<Map<String, String>>(){}),
                        rs.getString("VA_API_CHR"),
                        rs.getString("VA_API_NME"),
                        rs.getString("VA_API_DSC"),
                        conf
                );
                req = new ApiRequestServer(
                        apiReq,
                        Map.of("app", rs.getString("VA_API_APP"), "env", rs.getString("VA_API_ENV"))
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            req.getRequest().setBody(rs.getString("VA_API_BDY"));
            return req;
        });
        log.info("app={}, env={} ==> {} requests", app, env, list.size());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(String app, String env, @NonNull ApiRequest req) {

        var q = "INSERT INTO API_REQ(ID_REQ, VA_API_URI, VA_API_MTH, VA_API_HDR, VA_API_BDY, VA_API_CHR, "
                + "VA_API_NME, VA_API_DSC, VA_API_APP, VA_API_ENV, "
                + "VA_ASR_PRL, VA_ASR_STR, VA_ASR_ENB, VA_ASR_DBG, VA_ASR_EXL) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        long next = nextId("ID_REQ", "API_REQ");
        template.update(q, ps-> {
            try {
                ps.setLong(1, next);
                ps.setString(2, req.getUri());
                ps.setString(3, req.getMethod());
                ps.setString(4, mapper.writeValueAsString(req.getHeaders()));
                ps.setString(5, req.getBody());
                ps.setString(6, req.getCharset());
                ps.setString(7, req.getName());
                ps.setString(8, req.getDescription());
                ps.setString(9, app);
                ps.setString(10, env);
                ps.setBoolean(11, req.getConfiguration().isParallel());
                ps.setBoolean(12, req.getConfiguration().isStrict());
                ps.setBoolean(13, req.getConfiguration().isEnable());
                ps.setBoolean(14, req.getConfiguration().isDebug());
                ps.setString(15, mapper.writeValueAsString(req.getConfiguration().getExcludePaths()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("request added {}", req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(@NonNull int[] id){
        String q = "DELETE FROM API_REQ WHERE ID_REQ IN" + inArgs(id.length);
        template.update(q, ps-> {
            for(var i=0; i<id.length; i++) {
                ps.setInt(i+1, id[i]);
            }
        });
        log.info("");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(@NonNull int[] id, boolean state){

        String q = "UPDATE API_REQ SET VA_ASR_ENB = ? WHERE ID_REQ IN" + inArgs(id.length);
        template.update(q, ps-> {
            ps.setBoolean(1, state);
            for(var i=0; i<id.length; i++) {
                ps.setInt(i+2, id[i]);
            }
        });
    }

    private Long nextId(String col, String table) {
        return template.query("SELECT MAX(" + col + ") FROM " + table,
                rs-> rs.next() ? rs.getLong(1) : 0) + 1;
    }
}
