package org.usf.assertapi.server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TeradataPersister {

	public void setBoolean(PreparedStatement ps, int index, boolean b) throws SQLException {
		ps.setByte(index, (byte)(b ? 1 : 0));
	}

	public boolean getBoolean(ResultSet rs, String column) throws SQLException {
		return rs.getByte(column) == 1;
	}
}
