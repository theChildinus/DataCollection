### 配置问题
- 连接h2数据库需要注意工程中的h2-X.X.XXX.jar版本要和运行的h2数据库版本相同
### 一些SQL语句
- 从csv文件中导入数据到h2数据库中
    > `create table 表名 as select * from csvread('csv文件路径');`
- 查询表信息
    > `select * from 表名;`
- 添加列
    > `alter table 表名 add column 列名 varchar(30);`
- 修改某一列的数据
    > `UPDATE 表名称 SET 列名称=新值 WHERE 列名称=某值;`