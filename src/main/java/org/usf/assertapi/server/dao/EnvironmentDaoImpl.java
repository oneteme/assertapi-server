package org.usf.assertapi.server.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.ServerAuth;
import org.usf.assertapi.core.ServerConfig;
import org.usf.assertapi.server.model.ApiServerConfig;

import java.util.List;

import static org.usf.assertapi.server.utils.DaoUtils.inArgs;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EnvironmentDaoImpl implements EnvironmentDao {

    private final ObjectMapper mapper;
    private final JdbcTemplate template;

    @Override
    public List<ApiServerConfig> select() {
        String q = "SELECT ID_ENV, VA_API_HST, VA_API_PRT, VA_API_AUT_HST, VA_API_AUT_MTH, VA_API_APP, VA_API_ENV, VA_API_PRD "
                + "FROM API_ENV";
        var list = template.query(q, (rs, i) -> {
            var serverAuth = new ServerAuth();
            serverAuth.put("type", rs.getString("VA_API_AUT_MTH"));
            serverAuth.put("access-token-url", rs.getString("VA_API_AUT_HST"));
            var serverConfig = new ServerConfig();
            serverConfig.setAuth(serverAuth);
            serverConfig.setHost(rs.getString("VA_API_HST"));
            serverConfig.setPort(rs.getInt("VA_API_PRT"));
            return new ApiServerConfig(
                    rs.getLong("ID_ENV"),
                    serverConfig,
                    rs.getString("VA_API_APP"),
                    rs.getString("VA_API_ENV"),
                    rs.getBoolean("VA_API_PRD")
            );
        });
        log.info("{} environments", list.size());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(@NonNull ApiServerConfig serverConfig) {
        var q = "INSERT INTO API_ENV(ID_ENV, VA_API_HST, VA_API_PRT, VA_API_AUT_HST, VA_API_AUT_MTH, VA_API_APP, "
                + "VA_API_ENV, VA_API_PRD) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        long next = nextId("ID_ENV", "API_ENV");
        template.update(q, ps-> {
            ps.setLong(1, next);
            ps.setString(2, serverConfig.getServerConfig().getHost());
            ps.setInt(3, serverConfig.getServerConfig().getPort());
            ps.setString(4, serverConfig.getServerConfig().getAuth() == null ? null : serverConfig.getServerConfig().getAuth().getAccessTokenUrl());
            ps.setString(5, serverConfig.getServerConfig().getAuth() == null ? null : serverConfig.getServerConfig().getAuth().getAuthMethod());
            ps.setString(6, serverConfig.getApp());
            ps.setString(7, serverConfig.getEnv());
            ps.setBoolean(8, serverConfig.isProd());
        });
        log.info("Environment added {}", serverConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(@NonNull int[] ids) {
        String q = "DELETE FROM API_ENV WHERE ID_ENV IN" + inArgs(ids.length);
        template.update(q, ps-> {
            for(var i=0; i<ids.length; i++) {
                ps.setInt(i+1, ids[i]);
            }
        });
        log.info("");
    }

    private Long nextId(String col, String table) {
        return template.query("SELECT MAX(" + col + ") FROM " + table,
                rs-> rs.next() ? rs.getLong(1) : 0) + 1;
    }
}
