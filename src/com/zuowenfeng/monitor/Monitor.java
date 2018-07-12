package com.zuowenfeng.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.zuowenfeng.connection.viewcomposite.CenterErrOp;
import com.zuowenfeng.monitor.monitorDAO.StationDAO;

public class Monitor {
	private ServerSocket serverSocket;
	private int port = 9999;
	private ArrayList<Socket> sockets = new ArrayList<Socket> ();
	private ArrayList<String> ipArray = new ArrayList<String> ();
	private Map<String, Socket> socketIP = new HashMap<String, Socket> ();
	public Monitor() throws IOException {
		serverSocket = new ServerSocket(port);
		
		while ( true ) {
			Socket socket = serverSocket.accept();
			add(socket);
			//String ipaddress = so
			//InetAddress ipaddress = socket.getInetAddress();
			Thread thread1 = new Thread(new addConnection(socket) );
			thread1.start();
		}
		
	}
	
	public synchronized void add(Socket newSocket ) {
		sockets.add(newSocket);
		//location.add(locations);
	}
	
	public synchronized Socket get(int location) {
		return sockets.get(location);
	}
	
	public synchronized void remove(String ip) {
		sockets.remove(ip);
	}
	
	class addConnection implements Runnable {
		private Socket socket;
		
		public addConnection(Socket socket) {
			// TODO Auto-generated constructor stub
			this.socket = socket;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("New client " + socket.getInetAddress().toString() + " added.");
			int times = 0;
			
			System.out.println("Assigning tasks.");
			BufferedReader br;
			String anotherIP = "";

			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String msg = br.readLine();
				String[] splits = msg.split(" ");
				ipArray.add(splits[0]);
				StationDAO dao = new StationDAO();
				ResultSet sets = dao.getAssignedResult(new String[]{"dipaddress", "dport", "backup"}, "sipaddress='"+ splits[0] + "' and sport=" + splits[1] + ";");
				anotherIP = splits[0];
				
				while ( sets.next() ) {
					System.out.println(sets.getString("dipaddress") + ":" + sets.getString("dport") + " selected.");
					String backup = sets.getString("backup");
					
					if ( backup == null ) {
						PrintWriter pw = new PrintWriter(socket.getOutputStream(), true );
						System.out.println("No backups, This client can start now.");
						pw.println("NoBackups");
						socketIP.put(anotherIP, socket);
					}
					
					else {
						PrintWriter pw = new PrintWriter(socket.getOutputStream(), true );
						System.out.println("There exists backups. This client needs to wait.");
						pw.println("wait");
						
						for ( int i = 0; i <= ipArray.size() - 1; i++ ) {
							
							if ( ipArray.get(i).equals(backup)) {
								System.out.println(ipArray.get(i));
								pw = new PrintWriter(socketIP.get(ipArray.get(i)).getOutputStream(), true );
								pw.println("delete " + splits[0]);
								//BufferedReader br2 = new BufferedReader(new InputStreamReader(socketIP.get(ipArray.get(i)).getInputStream()));
								//String result = br2.readLine();
								Thread.sleep(5000);
								
								socketIP.remove(anotherIP);
								dao.updateAssignedResult(new String[]{"backup"}, new String[]{null}, "sipaddress='" + splits[0] + "' and sport=" + splits[1] + ";");
								pw = new PrintWriter(socket.getOutputStream(), true );
								pw.println("success");
								socketIP.put(anotherIP, socket);
								break;
							}
							
						}
						
						break;
					}
					
				}
				
			} catch (ClassNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while ( true ) {
				
				try {
					socket.setSoTimeout(10000);
					br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					br.readLine();
					times = 0;
					//System.out.println("Connection still exists.");
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					times += 1;
					System.out.println("Client " + socket.getInetAddress() + " out of connection " + times + " times.");
					
					if ( times > 3 ) {
						ipArray.remove(anotherIP);
						socketIP.remove(anotherIP);
						sockets.remove(socket);
						System.out.println("Client " + socket.getInetAddress() + "shutdown.");
						System.out.println("Starting alternate system.");
						System.out.println("Choosing a machine to alternate.");
						
						String[] ips = new String[ ipArray.size() ];
						
						for (int i = 0; i <= ipArray.size() - 1; i++ ) {
							ips[ i ] = ipArray.get(i);
						}
						
						CenterErrOp errorOps = new CenterErrOp( null, "Error Operation", anotherIP, ips );
						String changeIP = errorOps.getSelectedIP();
						System.out.println("Changing IP to machine origianl IP is " + changeIP );
						
						try {
							StationDAO dao = new StationDAO();
							dao.updateAssignedResult(new String[]{"backup"}, new String[]{changeIP}, "sipaddress = '" + anotherIP + "'");
							
							for ( int i = 0; i <= ipArray.size() - 1; i++ ) {
								
								if ( ipArray.get(i).equals(changeIP )) {
									PrintWriter pw = new PrintWriter(socketIP.get(ipArray.get(i)).getOutputStream(), true );
									pw.println("add " + anotherIP );
									socketIP.put(anotherIP, socketIP.get(ipArray.get(i)));
									break;
								}
							}
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						break;
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					times += 1;
					System.out.println("Client " + socket.getInetAddress() + " out of connection " + times + " times.");
					
					if ( times > 3 ) {
						ipArray.remove(anotherIP);
						socketIP.remove(anotherIP);
						sockets.remove(socket);
						System.out.println("Client " + socket.getInetAddress() + "shutdown.");
						System.out.println("Starting alternate system.");
						System.out.println("Choosing a machine to alternate.");
						
						String[] ips = new String[ ipArray.size() ];
						
						for (int i = 0; i <= ipArray.size() - 1; i++ ) {
							ips[ i ] = ipArray.get(i);
						}
						
						CenterErrOp errorOps = new CenterErrOp( null, "Error Operation", anotherIP, ips );
						String changeIP = errorOps.getSelectedIP();
						System.out.println("Changing IP to machine origianl IP is " + changeIP );
						
						try {
							StationDAO dao = new StationDAO();
							dao.updateAssignedResult(new String[]{"backup"}, new String[]{changeIP}, "sipaddress = '" + anotherIP + "'");
							
							for ( int i = 0; i <= ipArray.size() - 1; i++ ) {
								
								if ( ipArray.get(i).equals(changeIP )) {
									PrintWriter pw = new PrintWriter(socketIP.get(ipArray.get(i)).getOutputStream(), true );
									pw.println("add " + anotherIP );
									socketIP.put(anotherIP, socketIP.get(ipArray.get(i)));
									break;
								}
								
							}
							
						} catch (ClassNotFoundException ee) {
							// TODO Auto-generated catch block
							ee.printStackTrace();
						} catch (SQLException ee) {
							// TODO Auto-generated catch block
							ee.printStackTrace();
						} catch (IOException ee) {
							// TODO Auto-generated catch block
							ee.printStackTrace();
						}
						
						break;
					}
				}
				
			}
			
			System.out.println("Client " + socket.getInetAddress() + " has encountered a problem, connection will be shutdown.");
			
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//remove(locations - 1 );
		}
		
	}
	
	public static void main ( String[] args ) throws IOException {
		Monitor mon = new Monitor();
	}
	
}
