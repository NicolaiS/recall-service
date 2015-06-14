package kr.kaist.resl.recallservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import kr.kaist.resl.recallservice.model.RecallIssue;
import kr.kaist.resl.recallservice.model.RecallNotification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author NicolaiSonne
 *
 *         Registration singleton
 */
public class Registration {
	private static Registration instance = null;

	private volatile Gson gson;

	private volatile Map<String, List<Session>> urnSessionsMap = null;
	private volatile Map<Session, List<String>> sesssionUrnsMap = null;

	private Registration() {
		gson = new GsonBuilder().create();
		urnSessionsMap = new HashMap<String, List<Session>>();
		sesssionUrnsMap = new HashMap<Session, List<String>>();
	}

	public synchronized static Registration getInstance() {
		if (instance == null) {
			instance = new Registration();
		}
		return instance;
	}

	/**
	 * Register URNs to session
	 * 
	 * @param urns
	 * @param session
	 */
	public synchronized void register(List<String> urns, Session session) {
		if (sesssionUrnsMap.containsKey(session)) {
			unregister(session);
		}

		sesssionUrnsMap.put(session, urns);
		for (String urn : urns) {
			register(urn, session);
		}
		System.out.println("- Session " + session.getId() + " registered. "
				+ sesssionUrnsMap.size() + " sessions now registered");
	}

	/**
	 * Register URN to session
	 * 
	 * @param urn
	 * @param session
	 */
	public synchronized void register(String urn, Session session) {
		if (!urnSessionsMap.containsKey(urn))
			urnSessionsMap.put(urn, new ArrayList<Session>());
		urnSessionsMap.get(urn).add(session);
	}

	/**
	 * Unregister session
	 * 
	 * @param session
	 */
	public synchronized void unregister(Session session) {
		for (String urn : sesssionUrnsMap.get(session)) {
			if (urnSessionsMap.containsKey(urn)) {
				urnSessionsMap.get(urn).remove(session);
			}
		}
		sesssionUrnsMap.remove(session);
		System.out.println("- Session " + session.getId() + " unregistered. "
				+ sesssionUrnsMap.size() + " sessions now registered");
	}

	/**
	 * Send recall notification to all service registered to URN
	 * 
	 * @param urn
	 * @param ri
	 */
	public synchronized void send(String urn, RecallIssue ri) {
		RecallNotification rn = new RecallNotification(ri.getRecallSerial(),
				urn, ri.getIssueDate(), ri.getDescription(), ri.getDanger(),
				ri.getInstructions(), ri.getVersion());
		String rnJson = gson.toJson(rn);

		List<Session> sessions = urnSessionsMap.get(urn);
		if (sessions != null) {
			System.out.println("- Sending recall notification to "
					+ sessions.size() + " sessions");
			for (Session s : sessions) {
				try {
					s.getBasicRemote().sendText(rnJson);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
