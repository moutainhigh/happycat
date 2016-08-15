package com.woniu.sncp.cbss.core.model.access;

public class Limit {

	private String pnam;
	public int IPTC;
	public int IPLC;
	public int IPRT;
	public Boolean IPME;

	public String getPnam() {
		return pnam;
	}

	public void setPnam(String pnam) {
		this.pnam = pnam;
	}

	public int getIPTC() {
		return IPTC;
	}

	public void setIPTC(int iPTC) {
		IPTC = iPTC;
	}

	public int getIPLC() {
		return IPLC;
	}

	public void setIPLC(int iPLC) {
		IPLC = iPLC;
	}

	public int getIPRT() {
		return IPRT;
	}

	public void setIPRT(int iPRT) {
		IPRT = iPRT;
	}

	public Boolean getIPME() {
		return IPME;
	}

	public void setIPME(Boolean iPME) {
		IPME = iPME;
	}

	public String convertTo() {
		return "{\"IPTC\":" + getIPTC() + ",\"IPLC\":" + getIPLC() + ",\"IPRT\":" + getIPRT() + ",\"IPME\":\"" + getIPME() + "\"}";
	}


}
