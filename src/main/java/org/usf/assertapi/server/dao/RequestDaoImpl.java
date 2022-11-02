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
import org.usf.assertapi.core.AssertionConfig;
import org.usf.assertapi.server.model.ApiRequestGroupServer;
import org.usf.assertapi.server.model.ApiRequestServer;

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
    public List<ApiRequestServer> selectRequest(int[] ids, List<String> envs, String app) {
        List<Object> args = new LinkedList<>();
        StringBuilder q = new StringBuilder("SELECT API_REQ.ID_REQ, VA_API_URI, VA_API_MTH, VA_API_HDR, VA_API_BDY, VA_API_CHR,"
                + " VA_API_NME, VA_API_DSC, VA_ASR_PRL, VA_ASR_STR, VA_ASR_ENB, VA_ASR_DBG, VA_ASR_EXL, VA_API_APP, VA_API_ENV"
                + " FROM API_REQ INNER JOIN API_REQ_GRP ON API_REQ.ID_REQ = API_REQ_GRP.ID_REQ WHERE 1 = 1");

        if(!CollectionUtils.isEmpty(envs)) {
            q.append(" AND VA_API_ENV IN ").append(inArgs(envs.size()));
            args.addAll(envs);
        }

        if(app != null) {
            q.append(" AND VA_API_APP = ?");
            args.add(app);
        }

        if(ids != null) {
            q.append(" AND API_REQ.ID_REQ IN ").append(inArgs(ids.length));
            for (int id : ids) {
                args.add(id);
            }
        }

        q.append(" ORDER BY API_REQ.ID_REQ ASC");

        var list = template.query(q.toString(), args.toArray(), rs-> {
            long actualId = 0;
            List<ApiRequestServer> requestList = new ArrayList<>();
            ApiRequestServer request = null;
            while(rs.next()) {
                long nextId = rs.getLong("ID_REQ");;
                if(actualId != nextId) {
                    actualId = nextId;
                    try {
                        var conf = new AssertionConfig(
                                rs.getBoolean("VA_ASR_DBG"),
                                rs.getBoolean("VA_ASR_ENB"),
                                rs.getBoolean("VA_ASR_STR"),
                                rs.getBoolean("VA_ASR_PRL"),
                                mapper.readValue(rs.getString("VA_ASR_EXL"), String[].class)
                        );
                        var apiRequest = new ApiRequest(
                                actualId,
                                rs.getString("VA_API_URI"),
                                rs.getString("VA_API_MTH"),
                                mapper.readValue(rs.getString("VA_API_HDR"), new TypeReference<Map<String, String>>(){}),
                                rs.getString("VA_API_CHR"),
                                rs.getString("VA_API_NME"),
                                rs.getString("VA_API_DSC"),
                                conf
                        );
                        var apiRequestGroup = new ApiRequestGroupServer(
                                rs.getString("VA_API_APP"),
                                rs.getString("VA_API_ENV")
                        );
                        List<ApiRequestGroupServer> requestGroupList = new ArrayList<>();
                        requestGroupList.add(apiRequestGroup);
                        request = new ApiRequestServer(
                                apiRequest,
                                requestGroupList
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    request.getRequest().setBody(rs.getString("VA_API_BDY"));
                    requestList.add(request);
                } else {
                    var apiRequestGroup = new ApiRequestGroupServer(
                            rs.getString("VA_API_APP"),
                            rs.getString("VA_API_ENV")
                    );
                    Objects.requireNonNull(request).getRequestGroupList().add(apiRequestGroup);
                }
            }
            return requestList;
        });
        log.info("app={}, env={} ==> {} requests", app, envs, Objects.requireNonNull(list).size());
        return list;
    }

    @Override
    public void insertRequest(long id, @NonNull ApiRequest req) {

        var q = "INSERT INTO API_REQ(ID_REQ, VA_API_URI, VA_API_MTH, VA_API_HDR, VA_API_BDY, VA_API_CHR, "
                + "VA_API_NME, VA_API_DSC, "
                + "VA_ASR_PRL, VA_ASR_STR, VA_ASR_ENB, VA_ASR_DBG, VA_ASR_EXL) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            try {
                ps.setLong(1, id);
                ps.setString(2, req.getUri());
                ps.setString(3, req.getMethod());
                ps.setString(4, mapper.writeValueAsString(req.getHeaders()));
                ps.setString(5, req.getBody());
                ps.setString(6, req.getCharset());
                ps.setString(7, req.getName());
                ps.setString(8, req.getDescription());
                ps.setBoolean(9, req.getConfiguration().isParallel());
                ps.setBoolean(10, req.getConfiguration().isStrict());
                ps.setBoolean(11, req.getConfiguration().isEnable());
                ps.setBoolean(12, req.getConfiguration().isDebug());
                ps.setString(13, mapper.writeValueAsString(req.getConfiguration().getExcludePaths()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("request added : {}", req);
    }

    @Override
    public void updateRequest(@NonNull ApiRequest req) {

        var q = "UPDATE API_REQ SET VA_API_URI = ?, VA_API_MTH = ?, VA_API_BDY = ?, " +
                "VA_API_NME = ?, VA_API_DSC = ? " +
                "WHERE ID_REQ = ?";
        template.update(q, ps-> {
            ps.setString(1, req.getUri());
            ps.setString(2, req.getMethod());
            ps.setString(3, req.getBody());
            ps.setString(4, req.getName());
            ps.setString(5, req.getDescription());
            ps.setLong(6, req.getId());
        });
        log.info("request updated : {}", req);
    }

    @Override
    public void deleteRequest(@NonNull int[] id){
        String q = "DELETE FROM API_REQ WHERE ID_REQ IN" + inArgs(id.length);
        template.update(q, ps-> {
            for(var i=0; i<id.length; i++) {
                ps.setInt(i+1, id[i]);
            }
        });
        log.info("");
    }

    @Override
    public void insertRequestGroup(@NonNull long id, @NonNull List<ApiRequestGroupServer> requestGroupList) {
        var q = "INSERT INTO API_REQ_GRP(ID_REQ, VA_API_APP, VA_API_ENV)"
                + " VALUES(?,?,?)";
        template.batchUpdate(q, requestGroupList, requestGroupList.size(), (ps, r) -> {
            ps.setLong(1, id);
            ps.setString(2, r.getApp());
            ps.setString(3, r.getEnv());
        });
        log.info("requestGroup added : {}", requestGroupList);
    }

    @Override
    public void deleteRequestGroup(@NonNull long id){
        String q = "DELETE FROM API_REQ_GRP WHERE ID_REQ = ?";
        template.update(q, ps-> ps.setLong(1, id));
        log.info("");
    }

    @Override
    public void updateState(@NonNull int[] id, boolean state){

        String q = "UPDATE API_REQ SET VA_ASR_ENB = ? WHERE ID_REQ IN" + inArgs(id.length);
        template.update(q, ps-> {
            ps.setBoolean(1, state);
            for(var i=0; i<id.length; i++) {
                ps.setInt(i+2, id[i]);
            }
        });
    }

    @Override
    public Long nextId(String col, String table) {
        return template.query("SELECT MAX(" + col + ") FROM " + table,
                rs-> rs.next() ? rs.getLong(1) : 0) + 1;
    }
}
