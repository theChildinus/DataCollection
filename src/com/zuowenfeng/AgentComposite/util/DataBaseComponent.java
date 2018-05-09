package com.zuowenfeng.AgentComposite.util;

import java.io.FileNotFoundException;
import java.sql.Connection;

public interface DataBaseComponent {
	
	public Connection geth2() throws FileNotFoundException;
}
