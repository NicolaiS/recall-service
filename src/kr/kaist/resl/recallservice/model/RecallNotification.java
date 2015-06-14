package kr.kaist.resl.recallservice.model;

import java.util.Date;

/**
 * 
 * @author NicolaiSonne
 *
 *         Model of recall notification
 */
public class RecallNotification extends RecallBase {

	private String urn = null;

	public RecallNotification(String recallSerial, String urn, Date issueDate,
			String description, String danger, String instructions,
			Integer version) {
		super(recallSerial, issueDate, description, danger, instructions,
				version);
		this.urn = urn;
	}

	public String getUrn() {
		return urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;
	}
}
