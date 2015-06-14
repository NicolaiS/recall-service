package kr.kaist.resl.recallservice.model;

/**
 * 
 * @author NicolaiSonne
 *
 *         Tuple of URN and version number
 */
public class Tuple {

	private String u = null;
	private Integer v = null;

	protected Tuple(String u, Integer v) {
		super();
		this.u = u;
		this.v = v;
	}

	public String getURN() {
		return u;
	}

	public Integer getVersion() {
		return v;
	}

}
