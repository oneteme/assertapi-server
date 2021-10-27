package fr.enedis.teme.assertapi.server;

import static java.lang.String.join;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.enedis.teme.assertapi.core.HttpQuery;
import fr.enedis.teme.assertapi.core.HttpRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainService {

	private final JdbcTemplate template;
	
	public List<HttpQuery> data(String app, String env) {
		
		List<Object> args = new ArrayList<>();
		String q  = "SELECT VA_TST_DSC,	VA_TST_PRL, VA_TST_STR,	VA_TST_ENB, VA_TST_DBG,"
				+ " AC.ID_URI AS AC_ID, AC.VA_URI AS AC_URI, AC.VA_MTH AS AC_MTH, AC.VA_CHR AS AC_CHR, AC.VA_EXL AS AC_EXL,"
				+ " EX.ID_URI AS EX_ID,	EX.VA_URI AS EX_URI, EX.VA_MTH AS EX_MTH, EX.VA_CHR AS EX_CHR, EX.VA_EXL AS EX_EXL"
				+ " FROM API_TEST API"
				+ " INNER JOIN HTTP_URI AC ON API.ID_URI_ACT = AC.ID_URI"
				+ " INNER JOIN HTTP_URI EX ON API.ID_URI_EXT = EX.ID_URI"
				+ " WHERE 1=1";
		if(app != null) {
			q += " AND VA_API_APP=?";
			args.add(app);
		}
		if(env != null) {
			q += " AND VA_API_ENV=?";
			args.add(env);
		}
		return template.query(q, args.toArray(), (rs, i)->{
			HttpQuery hq = new HttpQuery();
			hq.setDescription(rs.getString("VA_TST_DSC"));
			hq.setParallel(rs.getBoolean("VA_TST_PRL"));
			hq.setStrict (rs.getBoolean("VA_TST_STR"));
			hq.setEnable(rs.getBoolean("VA_TST_ENB"));
			hq.setDebug(rs.getBoolean("VA_TST_DBG"));
			hq.setActual(requestMapper("AC").mapRow(rs, i));
			hq.setExpected(requestMapper("EX").mapRow(rs, i));
			return hq;
		});
	}
	
	private static RowMapper<HttpRequest> requestMapper(String prefix){
		return (rs, i)->{
			HttpRequest hr = new HttpRequest();
			hr.setUri(rs.getString(prefix + "_URI"));
			hr.setMethod(rs.getString(prefix + "_MTH"));
			hr.setCharset(rs.getString(prefix + "_CHR"));
			hr.setExcludePaths(ofNullable(rs.getString(prefix + "_EXL")).map(v-> v.split(",")).orElse(null));
			return hr;
		};
	}

	@Transactional(rollbackFor = Exception.class)
	public void insert(String app, String env, HttpQuery query) {
		
		int exId = nextId("ID_URI", "HTTP_URI");
		int acId = exId+1;
		String q = "INSERT INTO HTTP_URI(ID_URI, VA_URI, VA_MTH, VA_CHR, VA_EXL) VALUES(?,?,?,?,?)";
		template.update(q, psSetter(exId, query.getExpected()));
		template.update(q, psSetter(acId, query.getActual()));
		
		int nextId = nextId("ID_TST", "API_TEST");
		q = "INSERT INTO API_TEST(ID_TST, ID_URI_EXT, ID_URI_ACT, VA_TST_DSC, VA_TST_PRL, VA_TST_STR, VA_TST_ENB, VA_TST_DBG, VA_API_APP, VA_API_ENV) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
		template.update(q, ps-> {
			ps.setInt(1, nextId);
			ps.setInt(2, exId);
			ps.setInt(3, acId);
			ps.setString(4, query.getDescription());
			ps.setBoolean(5, query.isParallel());
			ps.setBoolean(6, query.isStrict());
			ps.setBoolean(7, query.isEnable());
			ps.setBoolean(8, query.isDebug());
			ps.setString(9, app);
			ps.setString(10, env);
		});
	}
	
	public void state(int[] id, boolean state){
		
		String q = "UPDATE API_TEST SET VA_TST_ENB = ? WHERE ID_TST IN" + inArgs(id.length);
		template.update(q, ps-> {
			ps.setBoolean(1, state);
			for(int i=0; i<id.length; i++) {				
				ps.setInt(i+2, id[i]);
			}
		});
	}
	
	void delete(int[] id){
		
		String q = "DELETE FROM API_TEST WHERE ID_TST IN" + inArgs(id.length);
		template.update(q, ps-> {
			for(int i=0; i<id.length; i++) {				
				ps.setInt(i+1, id[i]);
			}
		});
	}
	
	private static PreparedStatementSetter psSetter(int id, HttpRequest o) {
		return ps-> {
			ps.setInt(1, id);
			ps.setString(2, o.getUri());
			ps.setString(3, o.getMethod());
			ps.setString(4, o.getCharset());
			ps.setString(5, o.getExcludePaths() == null ? null : join(",", o.getExcludePaths()));
		};
	}
	
	private int nextId(String col, String table) {
		return template.query("SELECT MAX(" + col + ") FROM " + table, 
				rs-> rs.next() ? rs.getInt(1) : 0) + 1;
	}

	private static String inArgs(int n) {
		return "(" + (n == 1 ? "?" : "?" + ",?".repeat(n-1)) + ")";
	}
}
