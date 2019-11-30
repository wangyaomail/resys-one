# resys-one

## 各个模块说明

### resys-one-common
基础工程类，提供通用的dao和service，注意这里的dao和service只有以下几个不是abstract的：
- LocalCacheDao
- SysConfService
- TimerService
- QueueServer
- MainServer
这几个工具实现已完整，如果需要用可以在配置文件种启用这几种工具


### resys-one-batch
单次运行性服务，通过crontab设定定时任务执行

### resys-one-stream
长时性服务，接收和转发数据，不内置tomcat，需要提供系统自动启动的功能，且支持按需启动多个。
stream可以访问hadoop、hbase、hive、kafka、activemq，不可访问mongo

### resys-one-server
提供接口服务，内置tomcat，系统内只需要有一个，不能过多启动。
server可以访问mongo、couchbase、kafka、activemq，不可访问hadoop、hbase、hive

### resys-one-view
提供数据展示和测试页面


