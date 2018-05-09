package com.tianyue.opcclient;


/**
 * OpcClientAPI实现OPC通信相关功能
 * 每个OpcClientAPI实例对应某一个server-group组合
 * 如果需要访问多个server或group，请new多个opcInit实例并分别配置，并new多个OpcClientAPI实例分别处理）
 * 
 * @author tian
 *
 */
public class OpcClientAPI {
	
	public int connectionId;
	
	static {
		System.loadLibrary("opc");
	}
	/**
	 * 建立OPC连接
	 * @param IPAddress
	 * OPC服务器IP地址
	 * @param serverName
	 * OPC服务器名
	 * @param groupName
	 * OPC服务器组名，一般情况下为group0
	 * @return
	 */
	public native int connect(String IPAddress, String serverName,
			String groupName);
	/**
	 * 本地方法：添加OPC变量
	 * @param connectionId
	 * 用于区分与多个OPC服务器的连接
	 * @param itemName
	 * OPC变量名
	 * @param itemType
	 * 变量类型
	 * @param isActive
	 * 是否活动，此处应标记为活动(true)
	 * @return
	 */
	public native int addItem(int connectionId, String itemName, int itemType, boolean isActive);
    //以下read...Sync方法为底层实现读取的方法，已经在顶层结构中再次包装，不需要直接调用如下方法
	public native float readFloatSync(int connectionId, String itemName);
	public native double readDoubleSync(int connectionId, String itemName);
	public native int readIntSync(int connectionId, String itemName);
	public native boolean readBoolSync(int connectionId, String itemName);
	public native String readStringSync(int connectionId, String itemName);
	//以下get...Data方法为从底层结构中读取数据的方法，已经在顶层结构中再次包装，不需要直接调用如下方法
	public native float getFloatData(int connectionId, String itemName);
	public native double getDoubleData(int connectionId, String itemName);
	public native int getIntData(int connectionId, String itemName);
	public native boolean getBoolData(int connectionId, String itemName);
	public native String getStringData(int connectionId, String itemName);
	public native String getTimeStamp(int connectionId, String itemName);
	//以下write...Sync方法为调用底层写数据到服务器的方法
	public native int writeFloatSync(int connectionId, String itemName, float value);
	public native int writeDoubleSync(int connectionId, String itemName, double value);
	public native int writeIntSync(int connectionId, String itemName, int value);
	public native int writeBoolSync(int connectionId, String itemName, boolean value);
	public native int writeStringSync(int connectionId, String itemName, String value);
	
	public native void onReadComplete(float f);
	public native void shutdownServer(int connectionId);
	
	/**
	 * 由于需求要求刷新所有数据，故在底层做了修改：
	 * read...Sync函数返回的是参数中指定的变量名的值
	 * 但实际上该变量所在的服务器下的所有数据都已经更新到了底层的数据结构当中
	 * 之后只需要调用get...Data即可取得数据
	 * 所以用于全部刷新到底层的refreshAll方法的可以由读取一个OPC变量的方法实现
	 * @param connectionId
	 * 用于区分与多个OPC服务器的连接
	 * @param firstItemName
	 * 按之前添加顺序的第一个变量名
	 * @return
	 * 成功则返回第一个变量的值
	 * 失败返回-1
	 */
	public float refreshAll(int connectionId, String firstItemName){
		return readFloatSync(connectionId, firstItemName);
	}
	/**
	 * 测试用
	 * @param args
	 */
	public static void main(String args[]) {
		
		OpcClientAPI opc = new OpcClientAPI();
		System.out.println(opc.connect("127.0.0.1",
				"Matrikon.OPC.Simulation.1", "Group0"));
		opc.addItem(0, "testVar", 4, true);
		opc.writeFloatSync(0,"testVar", (float)345.3);
		//System.out.println(opc.addItem(0, "Random.Int2", 4, true));
		// System.out.println(opc.formItemList());
		//System.out.println(opc.readStringSync(0,"Bucket Brigade.String"));
		while (true){
			System.out.println("--------------------------------");
			System.out.println(opc.readFloatSync(0, "Random.Int1"));
			/*
			opc.refreshAll(0, "AP.ap0");
			for (int i=0;i<=200;i++){
				System.out.println("AP.ap"+String.valueOf(i));
				System.out.println(opc.getFloatData(0, "AP.ap"+String.valueOf(i)));
				//System.out.println(opc.readFloatSync(0, "AP.ap"+String.valueOf(i)));
			}
			*/
		    try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println(opc.getTimeStamp(0,"Random.Int2"));
		//System.out.println(opc.writeFloatSync(0,"Bucket Brigade.Real4", (float)345.3));
		
	}
}