package com.zuowenfeng.message;

import java.util.ArrayList;

public class RawMessage {
	private String date;
	private ArrayList<String> content;
	
	public RawMessage( String date, String contents ) {
		this.date = "" + date;
		this.content = new ArrayList<String> ();
		String first = "0x" + contents.substring(0, 2);
		this.content.add(first);
		
		for ( int i = 2; i <= contents.length() - 1; i += 2 ) {
			String result = "";
			
			if ( i + 2 <= contents.length() ) {
				result = "" + contents.substring(i, i + 2 );
			}
			
			else {
				result = "" + contents.substring(i, contents.length());
			}
			
			this.content.add(result);
		}
		
	}
	
	public String getDate() {
		return this.date;
	}
	
	public ArrayList<String> getContent() {
		return this.content;
	}
	
}
