package kr.kaist.resl.recallservice.model;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author NicolaiSonne
 *
 *         Model of recall issue
 */

public class RecallIssue extends RecallBase {

	private List<String> urns = null;

	public RecallIssue(String recallSerial, List<String> urns, Date issueDate,
			String description, String danger, String instructions,
			Integer version) {
		super(recallSerial, issueDate, description, danger, instructions,
				version);
		this.urns = urns;
	}

	public List<String> getUrns() {
		return urns;
	}

	public void setUrns(List<String> urns) {
		this.urns = urns;
	}

}
