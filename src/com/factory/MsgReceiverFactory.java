package com.factory;

import java.io.IOException;
import java.sql.SQLException;

import javax.swing.UnsupportedLookAndFeelException;

import com.zuowenfeng.connection.receivecomposite.MsgReceiver;

public class MsgReceiverFactory {
	public static MsgReceiver receiver;
	
	public void createMsgReceiverInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, UnsupportedLookAndFeelException, SQLException {
		receiver = new MsgReceiver();
	}
	
}
