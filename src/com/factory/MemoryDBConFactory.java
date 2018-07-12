package com.factory;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.configuration.MemoryDBConfiguration;

public class MemoryDBConFactory {
	public static MemoryDBConfiguration conf;
	
	public void createMemoryDBConfiguration() throws IOException {
		conf = new MemoryDBConfiguration();
	}
}
