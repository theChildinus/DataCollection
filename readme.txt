整个程序分为三个部分：配置部分，子站转发部分，数据处理部分。

配置部分：初始化所有连接需要配置的东西(部分配置可能未提取出来，需要在程序里面改)。
实现方式：所有的配置信息都存储在Configure2.txt里面，打开这个文件，可以看到里面需要配置的东西的形式。

Configuration是一个基类，它读取configure2.txt中的内容，然后将里面的配置文件读出来。同时，还实现了一个修改配置文件的函数。

该包中的其他类是根据具体的配置内容实现的类。它们都继承自Configuration。每一个具体的类里面都有相对应的成员配置属性。

子站转发部分的内容在com.liubao.substationprotocol.DAO中，ReceiveData运行在总站，用于接收数据和命令下发。SendData运行在子站，定时将实时库中的数据读取出来后，将数据直接直接通过SOCKET发送到总站。

ReceiveData调用MultiThreadsHandler中的run方法(多线程方式)。该方法在接收到数据后，调用checkDataType()函数查找数据库中对应的数据。然后调用checkOutbound()方法查看该数据是否超限。如果超限，就通过发布订阅系统将消息发送出去。然后，调用ObjectToUpdateMsg()函数生成更新数据库的语句并通过publishUpdate()函数将消息发布出去。然后，更新数据库。


数据处理部分的逻辑差不多。调用UpAnalogData()函数和UpDigitalData()函数来对底层传输上的数据进行处理。同样，首先通过CheckDataType()函数来查询是否存在对应的设备信息。通过isRedundant()函数判断消息是否冗余。通过isOutbound()判断消息是否超限，然后，调用ObjectToUpdateMsg()函数生成更新数据库的语句并通过publishUpdate()函数将消息发布出去。然后，更新数据库。


程序启动所需软件：管理员 servicemix Openldap messageReceiver coolsql h2 我的程序
