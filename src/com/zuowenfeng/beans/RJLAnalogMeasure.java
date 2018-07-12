package com.zuowenfeng.beans;

public class RJLAnalogMeasure extends RJLAnalog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected float highlimit;

	public float getHighlimit() {
		return highlimit;
	}

	public void setHighlimit(float highlimit) {
		this.highlimit = highlimit;
	}
	
	protected float lowlimit;

	public float getLowlimit() {
		return lowlimit;
	}

	public void setLowlimit(float lowlimit) {
		this.lowlimit = lowlimit;
	}
	
	protected float highhighlimit;

	public float getHighhighlimit() {
		return highhighlimit;
	}

	public void setHighhighlimit(float highhighlimit) {
		this.highhighlimit = highhighlimit;
	}
	
	protected float lowlowlimit;

	public float getLowlowlimit() {
		return lowlowlimit;
	}

	public void setLowlowlimit(float lowlowlimit) {
		this.lowlowlimit = lowlowlimit;
	}
	
	protected int outbound;

	public int getOutbound() {
		return outbound;
	}

	public void setOutbound(int outbound) {
		this.outbound = outbound;
	}
}
