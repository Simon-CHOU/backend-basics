
CITI015 STMT024
你用过什么Linux命令，你知道ulimit命令吗
你了解的Linux命令吗？

你的回答： “是的，我在后端开发和运维中经常使用各种Linux命令。让我按类别简单分享一些常用命令和实际应用。

首先，文件和目录管理：ls -la 查看详细列表，cd 切换目录，cat 和 tail -f 监控日志，比如调试服务时实时看错误；find 和 grep 搜索文件或内容，比如 grep -r 'error' 检查日志。

文本处理：vi 编辑文件，sed 和 awk 处理数据，比如 sed 's/old/new/g' 批量替换，awk '{print $1}' 提取列；uniq 去重，常跟 sort 一起用。

进程管理：ps aux | grep 查看进程，top 或 htop 监控资源，kill -9 终止，nohup & 后台运行服务。

网络和系统：curl 测试API，ss -tuln 检查端口，df -h 查看磁盘，free -m 看内存。

至于ulimit，我很熟悉，它是shell命令，用于设置进程资源限制，避免系统崩溃。在高并发Java服务中，我常用 ulimit -n 65535 增加文件描述符上限，防止 'too many open files' 错误；-s unlimited 优化栈大小。永久设置在 /etc/security/limits.conf 文件中，并在Docker中通过ulimits配置。这在生产环境中帮我优化了不少性能问题。

总之，这些命令在CI/CD管道、容器化和故障排除中都很关键。如果需要，我可以深入某个例子。”


> 用场景把命令串起来，好好整理下过去的排查经验。


CITI016
dockerfile 运行的原理，如何优化过多的layer

Dockerfile 是 Docker 构建镜像的核心脚本，其运行原理基于分层文件系统（layered filesystem），每个指令生成一个镜像层（layer）。

过多的Layer会导致镜像体积增大、构建时间延长和性能开销。
通过合并指令、多阶段构建（Mult-Stage Builds）和合理使用缓存来减少层数。

CITI017 STMT026
k8s pod启动原理，pod里的应用是如何启动的？

User -> API Server -> Scheduler -> Kubelet
Kubelet -> CRI: Create Sandbox -> Pull Image -> Start Container (App via CMD)
Kubelet -> Probes: Monitor & Restart

> lab: 观察一次pod的启动。

CITI018*
什么是ingress？有何业务价值？枚举应用场景？

Ingress作为Kubernetes流量管理的核心组件，在现代云原生架构中扮演着关键角色，是实现服务网格、API网关、负载均衡等功能的重要基础设施。

最佳实践建议
1. 选择合适的Ingress Controller : 根据需求选择Nginx、Traefik、Istio Gateway等
2. SSL证书自动化 : 使用cert-manager实现Let's Encrypt自动证书管理
3. 监控和日志 : 配置Ingress访问日志和性能监控
4. 安全加固 : 实施WAF规则、IP白名单、OAuth认证等
5. 高可用部署 : Ingress Controller多副本部署，避免单点故障

> lab: 有了ingress，容器化部署的 spring boot 后端微服务之间还需要curl ip:port/path 或者 domain/path 互访吗？（domain可能是公司内网SF区或者DMZ区可用的域名）


STMT026
k8s docker 掌握吗？

我在实际项目中深度使用Docker和K8s。让我从三个维度分享：日常开发、生产部署和团队协作。

docker
开发环境标准化：
├── Dockerfile最佳实践
│   ├── 多阶段构建减少镜像体积
│   ├── Layer优化（合并RUN指令）
│   └── 安全扫描和非root用户
├── Docker Compose本地开发
│   ├── 服务编排（数据库、Redis、MQ）
│   ├── 网络隔离和数据持久化
│   └── 环境变量管理
└── 镜像管理
    ├── 私有Registry集成
    ├── 镜像版本策略
    └── 构建缓存优化

k8s
应用部署：
├── 工作负载管理
│   ├── Deployment滚动更新
│   ├── StatefulSet有状态服务
│   └── Job/CronJob批处理
├── 服务发现与负载均衡
│   ├── Service类型选择
│   ├── Ingress路由规则
│   └── 服务网格考虑
├── 配置与存储
│   ├── ConfigMap/Secret管理
│   ├── PV/PVC持久化
│   └── 环境配置分离
└── 监控与故障排除
    ├── 健康检查配置
    ├── 日志聚合策略
    └── 资源限制调优

> lab: 总结docker-compose搭建开发环境、k9s连接测试环境看日志的技巧和配置。