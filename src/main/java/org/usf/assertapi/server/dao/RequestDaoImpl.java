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
import org.usf.assertapi.server.model.ApiRequestServer;
import org.usf.assertapi.server.utils.StringBuilder;

import java.io.IOException;
import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.usf.assertapi.core.Utils.*;
import static org.usf.assertapi.server.utils.DaoUtils.inArgs;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RequestDaoImpl implements RequestDao {

    private final ObjectMapper mapper;
    private final JdbcTemplate template;

    @Override
    public List<ApiRequest> selectRequest(int[] ids, String app, Set<String> envs) {

        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT DISTINCT O_REQ.ID_REQ, VA_URI, VA_MTH, VA_HDR, VA_BDY,"
                + " VA_NME, VA_DSC, VA_VRS, VA_PRL, VA_ENB, VA_STT, CNT_CMP"
                + " FROM O_REQ"
                + " INNER JOIN (SELECT ID_REQ, VA_APP, COUNT(ID_REQ) as nb FROM O_REQ_ENV WHERE 1 = 1");

        if(app != null) {
            q.append(" AND VA_APP = ?");
            args.add(app);
        }

        if(!CollectionUtils.isEmpty(envs)) {
            q.append(" AND VA_RLS IN ").append(inArgs(envs.size()));
            args.addAll(envs);
        }

        q.append(" GROUP BY ID_REQ, VA_APP HAVING COUNT(ID_REQ) >= ?");
        args.add(!CollectionUtils.isEmpty(envs) ? envs.size() : 0);
        q.append(" ) as GRP ON GRP.ID_REQ = O_REQ.ID_REQ");

        if(ids != null) {
            q.append(" WHERE O_REQ.ID_REQ IN ").append(inArgs(ids.length));
            for (int id : ids) {
                args.add(id);
            }
        }

        q.append(" ORDER BY VA_NME ASC");

        var list = template.query(q.toString(), args.toArray(), (rs, i) -> {

            try {
                ApiRequest request = new ApiRequest();
                request.setId(rs.getLong("ID_REQ"));
                request.setName(rs.getString("VA_NME"));
                request.setVersion(rs.getInt("VA_VRS"));
                request.setDescription(rs.getString("VA_DSC"));
                request.setUri(rs.getString("VA_URI"));
                request.setMethod(rs.getString("VA_MTH"));
                request.setHeaders(mapper.readValue(rs.getString("VA_HDR"), new TypeReference<Map<String, List<String>>>(){}));
                request.setBody(rs.getString("VA_BDY") != null ? rs.getString("VA_BDY").getBytes() : null);
                request.setAccept(Stream.of(rs.getString("VA_STT").split(",")).mapToInt(Integer::parseInt).toArray());
                request.setExecution(new ExecutionConfig(rs.getBoolean("VA_ENB"), rs.getBoolean("VA_PRL")));
                if(rs.getString("CNT_CMP") != null) {
                    request.setComparator(mapper.readValue(rs.getString("CNT_CMP"), new TypeReference<ModelComparator<?>>(){}));
                }
                return request;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("app={}, env={} ==> {} requests", app, envs, list.size());
        return list;
    }

    @Override
    public List<ApiRequestServer> selectRequest() {
        var requests = selectRequest(null, null, null);
        var requestServers = new ArrayList<ApiRequestServer>();
        if(!CollectionUtils.isEmpty(requests)) {
            template.query("SELECT ID_REQ, VA_APP, VA_RLS FROM O_REQ_ENV WHERE ID_REQ IN " + inArgs(requests.size()), requests.stream().map(ApiRequest::getId).toArray(Long[]::new), rs -> {
                var id = rs.getLong("ID_REQ");
                var requestServer = requestServers.stream().filter(r -> r.getRequest().getId() == id).findFirst();
                if(requestServer.isPresent()) {
                    requestServer.get().getReleases().add(rs.getString("VA_RLS"));
                } else {
                    requestServers.add(new ApiRequestServer(requests.stream().filter(r -> r.getId() == id).findFirst().orElseThrow(), rs.getString("VA_APP"), new ArrayList<>() {{add(rs.getString("VA_RLS"));}}));
                }
            });
        }
        return requestServers;
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
                ps.setString(5, req.bodyAsString());
                ps.setString(6, req.getName());
                ps.setString(7, req.getDescription());
                ps.setInt(8, req.getVersion() != null ? req.getVersion() : 1);
                ps.setBoolean(9, req.getExecution().isParallel());
                ps.setBoolean(10, req.getExecution().isEnabled());
                ps.setString(11, Arrays.stream(req.getAccept()).mapToObj(String::valueOf).collect(Collectors.joining(",")));
                ps.setString(12, isEmpty(req.getComparators()) ? null : mapper.writeValueAsString(req.getComparators()));
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
                ps.setString(3, req.bodyAsString());
                ps.setString(4, req.getName());
                ps.setString(5, req.getDescription());
                ps.setString(6, mapper.writeValueAsString(req.getHeaders()));
                ps.setInt(7, req.getVersion() != null ? req.getVersion() : 1);
                ps.setBoolean(8, req.getExecution().isParallel());
                ps.setBoolean(9, req.getExecution().isEnabled());
                ps.setString(10, Arrays.stream(req.getAccept()).mapToObj(String::valueOf).collect(Collectors.joining(",")));
                ps.setString(11, isEmpty(req.getComparators()) ? null : mapper.writeValueAsString(req.getComparators()));
                ps.setLong(12, id);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("request updated : {}", req);
    }

    @Override
    public void deleteRequest(int[] ids){
        String q = "DELETE FROM O_REQ WHERE ID_REQ IN " + inArgs(ids.length);
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
    public void deleteRequestGroup(int[] ids) {
        String q = "DELETE FROM O_REQ_ENV WHERE ID_REQ IN " + inArgs(ids.length);
        template.update(q, ps-> {
            for(var i = 0; i < ids.length; i++) {
                ps.setLong(i+1, ids[i]);
            }
        });
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
