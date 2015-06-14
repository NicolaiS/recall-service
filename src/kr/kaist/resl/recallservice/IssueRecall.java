package kr.kaist.resl.recallservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import kr.kaist.resl.recallservice.model.RecallIssue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Manufacturer interface. Allows manufacturers to publish recall notifications.
 * 
 * @author NicolaiSonne
 *
 */

@Path("/issuerecall")
public class IssueRecall {

	@POST
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public void issueRecall(String parameter) throws Exception {
		Gson gson = new GsonBuilder().create();

		System.out.println("- Recall issue received");

		// Decode parameter
		RecallIssue ri = gson.fromJson(parameter, RecallIssue.class);

		// Open DB connection
		Connection conn = null;
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		DataSource ds = (DataSource) envCtx.lookup("jdbc/recall_data");
		conn = ds.getConnection();

		// Insert recall issue and link to URNs
		Integer recallId = insertRecallIssue(conn, ri);
		linkToUrns(conn, ri.getUrns(), recallId);

		conn.close();

		// Send recall notification to all registered sessions of URNs
		for (String urn : ri.getUrns()) {
			Registration.getInstance().send(urn, ri);
		}
	}

	/**
	 * Insert recall date in database. Deletes old recall data with identical
	 * recall serial
	 * 
	 * @param conn
	 * @param ri
	 * @return
	 * @throws SQLException
	 */
	private Integer insertRecallIssue(Connection conn, RecallIssue ri)
			throws SQLException {

		System.out.println("- Inserting recall issue");
		System.out.println("-- " + ri.getRecallSerial());
		System.out.println("-- " + ri.getIssueDate().toLocaleString());
		System.out.println("-- " + ri.getDescription());
		System.out.println("-- " + ri.getDanger());
		System.out.println("-- " + ri.getInstructions());
		System.out.println("-- " + ri.getVersion());

		String sql = "DELETE FROM recall WHERE recall_serial = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, ri.getRecallSerial());
		ps.execute();
		ps.close();

		sql = "INSERT INTO recall (recall_serial,issue_date,description,danger,instructions,version) VALUES (?,?,?,?,?,?)";
		ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, ri.getRecallSerial());
		ps.setTimestamp(2, new Timestamp(ri.getIssueDate().getTime()));
		ps.setString(3, ri.getDescription());
		ps.setString(4, ri.getDanger());
		ps.setString(5, ri.getInstructions());
		ps.setInt(6, ri.getVersion());
		ps.execute();

		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		Integer id = rs.getInt(1);

		rs.close();
		ps.close();

		return id;
	}

	/**
	 * Link URNs to recall data
	 * 
	 * @param conn
	 * @param urns
	 * @param recallId
	 * @throws SQLException
	 */
	private void linkToUrns(Connection conn, List<String> urns, Integer recallId)
			throws SQLException {
		String sql;
		PreparedStatement ps;
		for (String urn : urns) {
			sql = "INSERT INTO urn (urn, recall_id) VALUES (?,?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, urn);
			ps.setInt(2, recallId);
			ps.execute();
			ps.close();
		}
	}

}
