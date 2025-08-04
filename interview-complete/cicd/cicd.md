





CITI015
你用过什么Linux命令，你知道ulimit命令吗

你的回答： “是的，我在后端开发和运维中经常使用各种Linux命令。让我按类别简单分享一些常用命令和实际应用。

首先，文件和目录管理：ls -la 查看详细列表，cd 切换目录，cat 和 tail -f 监控日志，比如调试服务时实时看错误；find 和 grep 搜索文件或内容，比如 grep -r 'error' 检查日志。

文本处理：vi 编辑文件，sed 和 awk 处理数据，比如 sed 's/old/new/g' 批量替换，awk '{print $1}' 提取列；uniq 去重，常跟 sort 一起用。

进程管理：ps aux | grep 查看进程，top 或 htop 监控资源，kill -9 终止，nohup & 后台运行服务。

网络和系统：curl 测试API，ss -tuln 检查端口，df -h 查看磁盘，free -m 看内存。

至于ulimit，我很熟悉，它是shell命令，用于设置进程资源限制，避免系统崩溃。在高并发Java服务中，我常用 ulimit -n 65535 增加文件描述符上限，防止 'too many open files' 错误；-s unlimited 优化栈大小。永久设置在 /etc/security/limits.conf 文件中，并在Docker中通过ulimits配置。这在生产环境中帮我优化了不少性能问题。

总之，这些命令在CI/CD管道、容器化和故障排除中都很关键。如果需要，我可以深入某个例子。”


> 用场景把命令串起来，好好整理下过去的排查经验。

