package kr.kaist.resl.recallservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import kr.kaist.resl.recallservice.model.RecallNotification;
import kr.kaist.resl.recallservice.model.Tuple;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@ServerEndpoint("/clientconnect")
public class ClientConnect {

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("- Session " + session.getId() + " opened");
	}

	@OnClose
	public void onClose(Session session) {
		System.out.println("- Session " + session.getId() + " closed");
		// Unregister clients when session closes
		Registration.getInstance().unregister(session);
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnMessage
	public void onMessage(Session session, String message) throws Exception {
		Gson gson = new GsonBuilder().create();

		// Decode parameters
		List<Tuple> params = gson.fromJson(message,
				new TypeToken<List<Tuple>>() {
				}.getType());

		List<String> urns = new ArrayList<String>(params.size());
		String log = "-- Session " + session.getId() + " registering:";
		for (Tuple p : params) {
			urns.add(p.getURN());
			log += "\n--- " + p.getURN() + " " + p.getVersion();
		}
		System.out.println(log);

		// Register session to URNs
		Registration.getInstance().register(urns, session);

		// Open DB connection
		Connection conn = null;
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		DataSource ds = (DataSource) envCtx.lookup("jdbc/recall_data");
		conn = ds.getConnection();

		String sql = "SELECT recall_serial,issue_date,description,danger,instructions,version "
				+ "FROM recall "
				+ "JOIN urn "
				+ "ON recall.recall_id=urn.recall_id "
				+ "WHERE urn.urn = ? AND recall.version > ?";

		Basic b = session.getBasicRemote();

		// Find unkown recall notifications. Return to client if found.
		System.out.println("-- Finding unknown recall notifications");
		boolean foundRN = false;
		for (Tuple p : params) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, p.getURN());
			ps.setInt(2, p.getVersion());
			ResultSet rs = ps.executeQuery();

			if (rs.first()) {
				foundRN = true;
				System.out.println("--- Returning recall notification for "
						+ p.getURN() + " v" + rs.getInt("version"));
				RecallNotification rn = new RecallNotification(
						rs.getString("recall_serial"), p.getURN(),
						rs.getTimestamp("issue_date"),
						rs.getString("description"), rs.getString("danger"),
						rs.getString("instructions"), rs.getInt("version"));
				b.sendText(gson.toJson(rn));
			}

			rs.close();
			ps.close();
		}

		if (!foundRN)
			System.out.println("--- None found");

		conn.close();
	}
}