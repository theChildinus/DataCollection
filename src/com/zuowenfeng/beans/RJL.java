package com.zuowenfeng.beans;

public class RJL extends HeatingPoint {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String community;
	protected String building;
	protected String resident;
	protected String description;
	
	public void setCommunity( String community ) {
		this.community = community;
	}
	
	public String getCommunity() {
		return this.community;
	}
	
	public void setBuilding( String building ) {
		this.building = building;
	}
	
	public String getBuilding() {
		return this.building;
	}
	
	public void setResident( String resident ) {
		this.resident = resident;
	}
	
	public String getResident() {
		return this.resident;
	}
	
	public void setDescription(String description ) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
}
