package com.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Configuration {
	protected FileInputStream fileInputStream;
	protected String[] configurationContent;
	
	public Configuration() throws IOException {
		fileInputStream = new FileInputStream("/home/kong/IdeaProjects/zuowenfeng/JinfangProject/Jinfang/configure2.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
		String line = "";
		String content = "";
		
		while ( ( line = br.readLine() ) != null ) {
			
			if ( line.startsWith("//") || line.startsWith("\n")) {
				
			}
			
			else {
				content = content.concat(line + "\n");
			}
			
		}
		
		configurationContent = content.split("\n");
		fileInputStream.close();
	}
	
	public void updateConf( ArrayList<String> columns, ArrayList<String> values ) throws IOException {
		FileWriter stream = new FileWriter("configure2.txt");
		BufferedWriter bw = new BufferedWriter(stream);
		String content = "";
		
		for ( int i = 0; i <= columns.size() - 1; i++ ) {
			content = content.concat(columns.get(i) + "=" + values.get(i) + "\n");
		}
		
		bw.write(content);
		bw.close();
		stream.close();
	} 
	 
}
