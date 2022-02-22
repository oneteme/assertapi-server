package fr.enedis.teme.assertapi.server;

import static java.sql.Timestamp.from;
import static java.sql.Types.BIGINT;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.enedis.teme.assertapi.core.ApiAssertionsResult;
import fr.enedis.teme.assertapi.core.ApiRequest;
import fr.enedis.teme.assertapi.core.AssertionConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StdDataPersister implements DataPersister {

	private final ObjectMapper mapper;
	private final JdbcTemplate template;
	
	@Override
	public List<ApiRequest> data(String app, String env) {
		
		List<Object> args = new LinkedList<>();
		String q= "SELECT ID_REQ, VA_API_URI, VA_API_MTH, VA_API_HDR, VA_API_BDY, VA_API_CHR, "
				+ "VA_API_NME, VA_API_DSC, VA_ASR_PRL, VA_ASR_STR, VA_ASR_ENB, VA_ASR_DBG, VA_ASR_EXL "
				+ "FROM API_REQ WHERE 1=1";
		if(app != null) {
			q += " AND VA_API_APP=?";
			args.add(app);
		}
		if(env != null) {
			q += " AND VA_API_ENV=?";
			args.add(env);
		}
		return template.query(q, args.toArray(), (rs, i)-> {
			ApiRequest req;
			try {
				var conf = new AssertionConfig(
						getBoolean(rs, "VA_ASR_DBG"), 
						getBoolean(rs, "VA_ASR_ENB"), 
						getBoolean(rs, "VA_ASR_STR"), 
						getBoolean(rs, "VA_ASR_PRL"), 
						mapper.readValue(rs.getString("VA_ASR_EXL"), String[].class));
				req = new ApiRequest(
						rs.getLong("ID_REQ"),
						rs.getString("VA_API_URI"),
						rs.getString("VA_API_MTH"),
						mapper.readValue(rs.getString("VA_API_HDR"), new TypeReference<Map<String, String>>(){}), 
						rs.getString("VA_API_CHR"),
						rs.getString("VA_API_NME"),
						rs.getString("VA_API_DSC"),
						conf);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			req.setBody(rs.getString("VA_API_BDY"));
			return req;
		});
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insert(String app, String env, @NonNull ApiRequest query) {
		
		var q = "INSERT INTO API_REQ(ID_REQ, VA_API_URI, VA_API_MTH, VA_API_HDR, VA_API_BDY, VA_API_CHR, "
				+ "VA_API_NME, VA_API_DSC, VA_API_APP, VA_API_ENV, "
				+ "VA_ASR_PRL, VA_ASR_STR, VA_ASR_ENB, VA_ASR_DBG, VA_ASR_EXL) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		long next = nextId("ID_REQ", "API_REQ");
		template.update(q, ps-> {
			try {
				ps.setLong(1, next);
				ps.setString(2, query.getUri());
				ps.setString(3, query.getMethod());
				ps.setString(4, mapper.writeValueAsString(query.getHeaders()));
				ps.setString(5, query.getBody());
				ps.setString(6, query.getCharset());
				ps.setString(7, query.getName());
				ps.setString(8, query.getDescription());
				ps.setString(9, app);
				ps.setString(10, env);
				setBoolean(ps, 11, query.getConfiguration().isParallel());
				setBoolean(ps, 12, query.getConfiguration().isStrict());
				setBoolean(ps, 13, query.getConfiguration().isEnable());
				setBoolean(ps, 14, query.getConfiguration().isDebug());
				ps.setString(15, mapper.writeValueAsString(query.getConfiguration().getExcludePaths()));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
	}
		
	@Override
	public void state(@NonNull int[] id, boolean state){
		
		String q = "UPDATE API_ASR SET VA_ASR_ENB = ? WHERE ID_TST IN" + inArgs(id.length);
		template.update(q, ps-> {
			setBoolean(ps, 1, state);
			for(var i=0; i<id.length; i++) {				
				ps.setInt(i+2, id[i]);
			}
		});
	}
	
	@Override
	public void delete(@NonNull int[] id){
		
		String q = "DELETE FROM API_ASR WHERE ID_ASR IN" + inArgs(id.length);
		template.update(q, ps-> {
			for(var i=0; i<id.length; i++) {				
				ps.setInt(i+1, id[i]);
			}
		});
	}

	@Override
	public void traceAll(@NonNull Collection<ApiAssertionsResult> list) {
		requireNonNull(list);
		var instant = Instant.now().truncatedTo(MILLIS);
		var q = "INSERT INTO API_ASR(ID_REQ, DH_ASR, VA_EXT_URL, VA_ACT_URL, VA_REQ_STT, VA_REQ_STP) VALUES(?,?,?,?,?,?)";
		template.batchUpdate(q, list, list.size(), (ps, result)-> {
			if(result.getQuery().getId() == null) {
				ps.setNull(1, BIGINT);
			}
			else {				
				ps.setLong(1, result.getQuery().getId());
			}
			ps.setTimestamp(2, from(instant));
			ps.setString(3, result.expectedUrl());
			ps.setString(4, result.actualUrl());
			ps.setString(5, result.getStatus().toString());
			ps.setString(6, result.getStep() == null ? null : result.getStep().toString());
		});
	}
	
	private Long nextId(String col, String table) {
		return template.query("SELECT MAX(" + col + ") FROM " + table, 
				rs-> rs.next() ? rs.getLong(1) : 0) + 1;
	}

	private static String inArgs(int n) {
		return "(" + (n == 1 ? "?" : "?" + ",?".repeat(n-1)) + ")";
	}

	/**
	 * resolve multiple database boolean implementation
	 * @param ps
	 * @param index
	 * @param b
	 * @throws SQLException
	 */
	public void setBoolean(PreparedStatement ps, int index, boolean b) throws SQLException {
		ps.setBoolean(index, b);
	}
	
	/**
	 * resolve multiple database boolean implementation
	 * @param rs
	 * @param index
	 * @return
	 * @throws SQLException 
	 */
	public boolean getBoolean(ResultSet rs, String column) throws SQLException {
		return rs.getBoolean(column);
	}
}
