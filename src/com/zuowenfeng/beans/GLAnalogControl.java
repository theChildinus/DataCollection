package com.zuowenfeng.beans;

import java.io.Serializable;

public class GLAnalogControl extends GLAnalog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int wordType = -1;
	
	public int getWordType() {
		return wordType;
	}
	
	public void setWordType(int wordType) {
		this.wordType = wordType;
	}
	
}
