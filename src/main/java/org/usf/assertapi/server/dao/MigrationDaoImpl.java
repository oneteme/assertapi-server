package org.usf.assertapi.server.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.usf.assertapi.server.model.ApiMigration;

import static java.util.Objects.requireNonNull;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MigrationDaoImpl implements MigrationDao {
    private final ObjectMapper mapper;
    private final JdbcTemplate template;

    @Override
    public void insertMigration(int id, @NonNull ApiMigration migration) {

        var q = "INSERT INTO O_REQ_MIG(ID_MIG, ID_REQ_1, ID_REQ_2, CNT_CMP"
                + " VALUES(?,?,?,?)";
        template.update(q, ps-> {
            try {
                ps.setLong(1, id);
                ps.setLong(2, migration.getIdReqOne());
                ps.setLong(3, migration.getIdReqTwo());
                ps.setString(4, mapper.writeValueAsString(migration.getContentComparator()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("migration added : {}", migration);
    }

    @Override
    public void updateMigration(int id, @NonNull ApiMigration migration) {
        var q = "UPDATE O_REQ_MIG SET CNT_CMP = ?"
                + " WHERE ID_MIG = ?";
        template.update(q, ps-> {
            try {
                ps.setString(1, mapper.writeValueAsString(migration.getContentComparator()));
                ps.setLong(2, id);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("migration updated : {}", migration);
    }

    @Override
    public Integer nextId(@NonNull String col, @NonNull String table) {
        return requireNonNull(template.query("SELECT MAX(" + col + ") FROM " + table,
                rs-> rs.next() ? rs.getInt(1) : 0)) + 1;
    }
}
