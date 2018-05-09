package com.zuowenfeng.beans;

import java.io.Serializable;

public class GL extends HeatingPoint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String boilerRoom;
	protected String boiler;
	protected String description;
	
	public void setBoilerRoom( String boilerRoom ) {
		this.boilerRoom = boilerRoom;
	}
	
	public String getBoilerRoom() {
		return this.boilerRoom;
	}
	
	public void setBoiler( String boiler ) {
		this.boiler = boiler;
	}
	
	public String getBoiler() {
		return this.boiler;
	}
	
	public void setDescription( String description ) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
}
