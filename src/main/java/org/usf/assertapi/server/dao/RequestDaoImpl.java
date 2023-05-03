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
import org.usf.assertapi.core.*;
import org.usf.assertapi.server.model.ApiMigration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Objects.requireNonNull;
import static org.usf.assertapi.core.Utils.defaultMapper;
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
        StringBuilder q = new StringBuilder("SELECT DISTINCT O_REQ.ID_REQ, VA_URI, VA_MTH, VA_HDR, VA_BDY,"
                + " VA_NME, VA_DSC, VA_VRS, VA_PRL, VA_ENB, VA_STT, CNT_CMP"
                + " FROM O_REQ"
                + " INNER JOIN (SELECT ID_REQ, VA_APP, VA_RLS, COUNT(ID_REQ) as nb FROM O_REQ_ENV GROUP BY ID_REQ, VA_APP, VA_RLS HAVING nb >= ")
                .append(!CollectionUtils.isEmpty(envs) ? envs.size() : 0)
                .append(" ) as GRP ON GRP.ID_REQ = O_REQ.ID_REQ WHERE 1 = 1");

        if(ids != null) {
            q.append(" AND O_REQ.ID_REQ IN ").append(inArgs(ids.length));
            for (int id : ids) {
                args.add(id);
            }
        }

        if(app != null) {
            q.append(" AND VA_APP = ?");
            args.add(app);
        }

        if(!CollectionUtils.isEmpty(envs)) {
            q.append(" AND VA_RLS IN ").append(inArgs(envs.size()));
            args.addAll(envs);
        }

        q.append(" ORDER BY VA_NME ASC");

        var list = template.query(q.toString(), args.toArray(), (rs, i) -> {
            ApiRequest request = null;
            try {
                var conf = new ExecutionConfig(
                        rs.getBoolean("VA_ENB"),
                        rs.getBoolean("VA_PRL")
                );
                request = new ApiRequest(
                        rs.getLong("ID_REQ"),
                        rs.getString("VA_NME"),
                        rs.getInt("VA_VRS"),
                        rs.getString("VA_DSC"),
                        rs.getString("VA_URI"),
                        rs.getString("VA_MTH"),
                        mapper.readValue(rs.getString("VA_HDR"), new TypeReference<Map<String, List<String>>>(){}),
                        rs.getString("VA_BDY") != null ? rs.getString("VA_BDY").getBytes() : null,
                        null,
                        Stream.of(rs.getString("VA_STT").split(",")).mapToInt(Integer::parseInt).toArray(),
                        conf,
                        mapper.readValue(rs.getString("CNT_CMP"), new TypeReference<ContentComparator<?>>(){}),// response config => json column
                        null, // stable reference
                        null
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
    public void insertRequest(int id, @NonNull ApiRequest req) {

        var q = "INSERT INTO O_REQ(ID_REQ, VA_URI, VA_MTH, VA_HDR, VA_BDY,"
                + " VA_NME, VA_DSC, VA_VRS, VA_PRL, VA_ENB, VA_STT, CNT_CMP)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            try {
                ps.setLong(1, id);
                ps.setString(2, req.getUri());
                ps.setString(3, req.getMethod());
                ps.setString(4, mapper.writeValueAsString(req.getHeaders()));
                ps.setString(5, req.getBody() != null ? new String(req.getBody(), UTF_8) : null);
                ps.setString(6, req.getName());
                ps.setString(7, req.getDescription());
                ps.setInt(8, req.getVersion());
                ps.setBoolean(9, req.getExecutionConfig().isParallel());
                ps.setBoolean(10, req.getExecutionConfig().isEnabled());
                ps.setString(11, Arrays.stream(req.getAcceptableStatus()).mapToObj(String::valueOf).collect(Collectors.joining(",")));
                ps.setString(12, mapper.writeValueAsString(req.getContentComparator()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("request added : {}", req);
    }

    @Override
    public void updateRequest(int id, @NonNull ApiRequest req) {

        var q = "UPDATE O_REQ SET VA_URI = ?, VA_MTH = ?, VA_BDY = ?, VA_NME = ?, VA_DSC = ?,"
                + " VA_HDR = ?, VA_VRS = ?, VA_PRL = ?, VA_ENB = ?, VA_STT = ?, CNT_CMP = ?"
                + " WHERE ID_REQ = ?";
        template.update(q, ps-> {
            try {
                ps.setString(1, req.getUri());
                ps.setString(2, req.getMethod());
                ps.setString(3, new String(req.getBody(), UTF_8));
                ps.setString(4, req.getName());
                ps.setString(5, req.getDescription());
                ps.setString(6, mapper.writeValueAsString(req.getHeaders()));
                ps.setInt(7, req.getVersion());
                ps.setBoolean(8, req.getExecutionConfig().isParallel());
                ps.setBoolean(9, req.getExecutionConfig().isEnabled());
                ps.setString(10, Arrays.toString(req.getAcceptableStatus()));
                ps.setString(11, mapper.writeValueAsString(req.getContentComparator()));
                ps.setLong(12, id);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("request updated : {}", req);
    }

    @Override
    public void deleteRequest(int[] ids){
        String q = "DELETE FROM O_REQ WHERE ID_REQ IN" + inArgs(ids.length);
        template.update(q, ps-> {
            for(var i=0; i<ids.length; i++) {
                ps.setLong(i+1, ids[i]);
            }
        });
        log.info("");
    }

    @Override
    public void insertRequestGroup(int id, @NonNull String app, @NonNull List<String> releases) {
        var q = "INSERT INTO O_REQ_ENV(ID_REQ, VA_APP, VA_RLS)"
                + " VALUES(?,?,?)";
        template.batchUpdate(q, releases, releases.size(), (ps, r) -> {
            ps.setLong(1, id);
            ps.setString(2, app);
            ps.setString(3, r);
        });
        log.info("requestGroup added : app={}, envs={}", app, releases);
    }

    @Override
    public void deleteRequestGroup(int id) {
        String q = "DELETE FROM O_REQ_ENV WHERE ID_REQ = ?";
        template.update(q, ps-> ps.setLong(1, id));
        log.info("");
    }

    @Override
    public void updateState(int[] ids, boolean state) {

        String q = "UPDATE O_REQ SET VA_ENB = ? WHERE ID_REQ IN" + inArgs(ids.length);
        template.update(q, ps-> {
            ps.setBoolean(1, state);
            for(var i=0; i<ids.length; i++) {
                ps.setInt(i+2, ids[i]);
            }
        });
    }

    @Override
    public Integer nextId(@NonNull String col, @NonNull String table) {
        return requireNonNull(template.query("SELECT MAX(" + col + ") FROM " + table,
                rs-> rs.next() ? rs.getInt(1) : 0)) + 1;
    }
}
