package com.zuowenfeng.beans;

import java.io.Serializable;

public class GLAnalogMeasure extends GLAnalog implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected float highlimit = Float.MAX_VALUE;

	public float getHighlimit() {
		return highlimit;
	}

	public void setHighlimit(float highlimit) {
		this.highlimit = highlimit;
	}
	
	protected float lowlimit = Float.MIN_VALUE;

	public float getLowlimit() {
		return lowlimit;
	}

	public void setLowlimit(float lowlimit) {
		this.lowlimit = lowlimit;
	}
	
	protected float highhighlimit = Float.MAX_VALUE;

	public float getHighhighlimit() {
		return highhighlimit;
	}

	public void setHighhighlimit(float highhighlimit) {
		this.highhighlimit = highhighlimit;
	}
	
	protected float lowlowlimit = Float.MIN_VALUE;

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
