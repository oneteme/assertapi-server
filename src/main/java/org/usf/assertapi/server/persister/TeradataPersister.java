package org.usf.assertapi.server.persister;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.usf.assertapi.server.persister.StdDataPersister;

public class TeradataPersister extends StdDataPersister {

	public TeradataPersister(ObjectMapper mapper, JdbcTemplate template) {
		super(mapper, template);
	}
	
	@Override
	public void setBoolean(PreparedStatement ps, int index, boolean b) throws SQLException {
		ps.setByte(index, (byte)(b ? 1 : 0));
	}

	@Override
	public boolean getBoolean(ResultSet rs, String column) throws SQLException {
		return rs.getByte(column) == 1;
	}

}
