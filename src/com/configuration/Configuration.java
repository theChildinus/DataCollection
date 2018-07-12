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
	public static boolean wholeStation;
	
	public Configuration() throws IOException {
	    fileInputStream = null;
	    if (!wholeStation) {
<<<<<<< HEAD
	        fileInputStream = new FileInputStream("/home/kong/IdeaProjects/zuowenfeng/JinfangProject/Jinfang/configure2.txt");
=======
	        fileInputStream = new FileInputStream("/home/vm/Jinfang/configure2.txt");
>>>>>>> 1b7d84e29fa02ddcb038401e9bf26a155b530c48
        } else {
	        fileInputStream = new FileInputStream("/home/kong/IdeaProjects/zuowenfeng/JinfangProject/Jinfang/configure2.txt");
        }
		BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
		String line = "";
		String content = "";
		
		while ( ( line = br.readLine() ) != null ) {
			
			if (line.startsWith("//") || line.startsWith("\n") || line.isEmpty()) {
			}
			else {
				content = content.concat(line + "\n");
			}
			
		}

		configurationContent = content.split("\n");
		fileInputStream.close();
	}
	
	public void updateConf( ArrayList<String> columns, ArrayList<String> values ) throws IOException {
		FileWriter stream = null;
        if (!wholeStation) {
<<<<<<< HEAD
            stream = new FileWriter("/home/kong/IdeaProjects/zuowenfeng/JinfangProject/Jinfang/configure2.txt");
=======
            stream = new FileWriter("/home/vm/Jinfang/configure2.txt");
>>>>>>> 1b7d84e29fa02ddcb038401e9bf26a155b530c48
        } else {
            stream = new FileWriter("/home/kong/IdeaProjects/zuowenfeng/JinfangProject/Jinfang/configure2.txt");
        }
		BufferedWriter bw = new BufferedWriter(stream);
		String content = "";

		String firstFifthChar = columns.get(0).substring(0, 5);
		for ( int i = 0; i <= columns.size() - 1; i++ ) {
            String tmp = columns.get(i).substring(0, 5);
            if (!tmp.equals(firstFifthChar)) {
                content = content.concat("\n");
                firstFifthChar = tmp;
            }
			content = content.concat(columns.get(i) + "=" + values.get(i) + "\n");
		}
		
		bw.write(content);
		bw.close();
		stream.close();
	} 
	 
}
