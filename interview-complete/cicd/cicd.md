
SANO010
我们如果发现了我们的Container启动是非常非常慢的，每一次发布到kubernetes可能要2分钟。你用什么方法可以做到最最最的启动时间。任何的方法可以用的，不只是说从代码里，你的Docker也可以改变，啥都可以改的。

应用层优化、容器镜像优化、Kubernetes配置优化，以及基础设施优化

诊断耗时：用kubectl describe pod和docker stats先确定是哪个环节最耗时，然后按影响程度优先解决

"最快见效的是镜像优化。我会采用多阶段构建，将构建依赖和运行时分离；使用Alpine Linux基础镜像减少体积；合并RUN指令减少层数；利用.dockerignore排除无关文件。同时配置镜像缓存策略，在每个节点预拉取常用基础镜像。"
"应用层面，我会启用Spring Boot的lazy initialization，只初始化必要的bean；使用GraalVM Native Image实现秒级启动；优化数据库连接池配置，避免启动时的连接等待。"

"K8s配置上，我会调整ImagePullPolicy为IfNotPresent，避免重复拉取；配置合理的resource limits和requests，防止资源竞争；使用readiness probe而不是liveness probe来判断启动完成；考虑使用DaemonSet预热节点。"

"长期来看，我会推动架构改进：实施蓝绿部署减少用户感知的启动时间；使用Istio的流量管理实现渐进式发布；考虑将有状态服务拆分，使用StatefulSet；建立完整的监控体系，持续优化启动性能。"

"作为技术负责人，我还会建立启动时间的SLA指标，定期review和优化；制定标准化的Docker最佳实践文档；培训团队成员掌握这些优化技巧，确保整个团队都能写出高性能的容器化应用。"

> lab: cold start latency


SANO011
你比较以下maven 和gradle ，你最多用的是哪一个，然后比较它们的异同
对比：
性能：gradle胜出：增量构建、构建缓存机制，在大型项目构建中优势明显
配置管理：maven xml 依赖直观，但是自定义构建逻辑时冗长。gradle DSL学习曲线更陡峭，但是在处理复杂多模块项目和复杂依赖关系中有优势。
生态成熟度：maven久经考验，更成熟。但gradle也已经足够成熟，被spring boot官方推荐。

> lab： 找一个大型开源项目，对比 maven 和 gradle 的速度、配置复杂度
lab: 学习写DSL的最小知识 groovy kotlin


SANO015
从你项目的实际经验来看，比如说你代码写了提交到Git上面，后面CI/CD 怎么做？

实际情况分析
真正的CI/CD部分：

- Git代码提交触发Jenkins流水线 ✅ (CI)
- 自动化构建打包 ✅ (CI)
- 代码静态扫描(Fortify, SonarQube) ✅ (CI)
- 自动化部署到生产环境 ✅ (CD)
传统IT流程部分：

- 手动补充测试报告
- 手动补充安全渗透测试报告
- 上线签报审批流程
- 人工协调运维部署



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