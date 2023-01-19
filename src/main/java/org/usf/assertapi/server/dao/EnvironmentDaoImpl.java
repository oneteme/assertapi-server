package org.usf.assertapi.server.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.usf.assertapi.core.ServerAuth;
import org.usf.assertapi.core.ServerConfig;
import org.usf.assertapi.server.model.Environment;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.usf.assertapi.server.utils.DaoUtils.inArgs;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EnvironmentDaoImpl implements EnvironmentDao {

    private final ObjectMapper mapper;
    private final JdbcTemplate template;

    @Override
    public List<Environment> selectEnvironment() {
        String q = "SELECT ID_ENV, VA_HST, VA_PRT, VA_APP, VA_RLS, FL_PRD, AUT_CNF"
                + " FROM R_ENV";
        var list = template.query(q, (rs, i) -> {
            try {
                var serverConfig = new ServerConfig(
                        rs.getString("VA_HST"),
                        rs.getInt("VA_PRT"),
                        mapper.readValue(rs.getString("AUT_CNF"), new TypeReference<ServerAuth>(){})
                );
                return new Environment(
                        rs.getLong("ID_ENV"),
                        rs.getString("VA_APP"),
                        rs.getString("VA_RLS"),
                        rs.getBoolean("FL_PRD"),
                        serverConfig
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("{} environments", list.size());
        return list;
    }

    @Override
    public void insertEnvironment(int id, @NonNull Environment environment) {
        var q = "INSERT INTO R_ENV(ID_ENV, VA_HST, VA_PRT, VA_APP, VA_RLS, FL_PRD, AUT_CNF)"
                + " VALUES(?,?,?,?,?,?,?)";
        template.update(q, ps-> {
            try {
                ps.setLong(1, id);
                ps.setString(2, environment.getServerConfig().getHost());
                ps.setInt(3, environment.getServerConfig().getPort());
                ps.setString(4, environment.getApp());
                ps.setString(5, environment.getRelease());
                ps.setBoolean(6, environment.isProd());
                ps.setString(7, mapper.writeValueAsString(environment.getServerConfig().getAuth()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("Environment added {}", environment);
    }

    @Override
    public void updateEnvironment(int id, @NonNull Environment environment) {

        var q = "UPDATE R_ENV SET VA_HST = ?, VA_PRT = ?,"
                + " VA_APP = ?, VA_RLS = ?, FL_PRD = ?, AUT_CNF = ?"
                + " WHERE ID_ENV = ?";
        template.update(q, ps-> {
            try {
                ps.setString(1, environment.getServerConfig().getHost());
                ps.setInt(2, environment.getServerConfig().getPort());
                ps.setString(3, environment.getServerConfig().getAuth() != null ? environment.getServerConfig().getAuth().getAccessTokenUrl() : null);
                ps.setString(4, environment.getServerConfig().getAuth() != null ? environment.getServerConfig().getAuth().getAuthMethod() : null);
                ps.setString(5, environment.getApp());
                ps.setString(6, environment.getRelease());
                ps.setBoolean(7, environment.isProd());
                ps.setString(8, mapper.writeValueAsString(environment.getServerConfig().getAuth()));
                ps.setLong(9, environment.getId());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("environment updated : {}", environment);
    }

    @Override
    public void deleteEnvironment(int[] ids) {
        String q = "DELETE FROM R_ENV WHERE ID_ENV IN" + inArgs(ids.length);
        template.update(q, ps-> {
            for(var i=0; i<ids.length; i++) {
                ps.setLong(i+1, ids[i]);
            }
        });
        log.info("");
    }

    @Override
    public Integer nextId(@NonNull String col, @NonNull String table) {
        return requireNonNull(template.query("SELECT MAX(" + col + ") FROM " + table,
                rs-> rs.next() ? rs.getInt(1) : 0)) + 1;
    }
}
