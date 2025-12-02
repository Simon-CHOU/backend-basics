# DEVLOG - MQ Labs 验证日志

## 错误和改正方案记录（倒序）

### 2025-12-02 19:06:00
**错误**: Java测试类放置在项目根目录，违反Maven标准结构
**改正方案**: 将TestDBConnection.java移动到src/test/java/com/example/mqlabs/目录下

### 2025-12-02 19:05:00
**错误**: PowerShell终端中docker命令无法识别
**改正方案**: 提供本地执行命令清单，用户需要在本地PowerShell/WSL终端中执行

### 2025-12-02 19:04:00  
**错误**: Maven exec插件参数格式错误
**改正方案**: 使用正确的Maven exec插件语法或直接运行Spring Boot应用

### 2025-12-02 19:03:00
**错误**: Java类名包含连字符导致编译错误
**改正方案**: 将类名改为使用驼峰命名法（test-db-connection → TestDBConnection）

## Lab验证状态

### Lab01 - Outbox Pattern基础
- 状态: 待验证
- 预期: 用户创建后，outbox表应有记录，RabbitMQ应收到消息

### Lab02 - Kafka事务消息
- 状态: 待验证  
- 预期: 用户创建后，kafka_inbox应有记录，Kafka应发送通知

### Lab03 - RocketMQ延迟消息
- 状态: 待验证
- 预期: 用户状态更新后，rocket_inbox应有记录，延迟10秒后发送通知

### Lab04 - 消息顺序性保证
- 状态: 待验证
- 预期: 相同key的消息按顺序处理，lab04_counts表记录正确

### Lab05 - 死信队列处理
- 状态: 待验证
- 预期: 失败消息进入DLQ，重试后成功处理

### Lab06 - 消息幂等性
- 状态: 待验证
- 预期: 重复消息只处理一次，lab06_counts表计数正确

### Lab07 - 实例路由
- 状态: 待验证
- 预期: 消息根据instanceId路由到特定实例处理
