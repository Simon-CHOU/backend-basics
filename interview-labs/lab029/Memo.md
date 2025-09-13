背景信息：
我原来做过公司的一个spring boot 业务系统后端开发。
我记忆中，公司的框架里，有一个类似  UserUtil.getUserInfo() 的静态工具类公共方法，可以在spring boot 服务中的controller和service等等几乎全局（只要这里业务逻辑是前端登录用户进来）获取到登录用户的信息。

这个信息是用户登录后登录方法存到 redis里面的。
登录用户请求是，会带着Header Cookie: token，其中token是一个uuid，是用户本次回话有效登录态的标记。

spring boot 服务会有一个命名类似 AccessInterceptor的拦截器，把header -cookie 中的token拿到，然后从redis getValue得到JSON，再反序列化得到UserInfo。然后好像是放到了 ThreadLocal 还是 transmittable-thread-local 成员变量了里，我记不清了。

这个设计广泛存在于前司趁机集团的几乎所有Java Spring Boot后端代码中。

目标：
理论:请问，这个全局getUserInfo获取用户信息的设计是一种什么样的best practice，有专门的形容吗？
实践：请设计一个实验，创建一个上述设计带spring boot 的最小化服务（MVP）,完整实现上述的需求。

构建要求：
我的记忆可能不准确，如果你知道我在说什么，可以用行业公认的最佳实践，而非完全遵照我回忆的字面描述。
编码遵循TDD 和 Clean Code的记录。
创建spring boot 项目时请按照 start.spring.io 的规范来。
前端模拟登录需要一个最小化的登录页面，请在当前目录下新建两个目录 XXX-fe代表前端（frontend）XXX-be代表后端（backend），其中XXX为你拟定的合理项目名
前端用 reactjs + typescript 等现代化前端技术，实现最小化的登录页面，用户名密码即可，登录后跳转到首页，首页用一个banner占位即可，不必有负责元素。
样式朴素到和html 裸标签一样，千万不要引入过多的样式和其他npm依赖。
前后端分离。
存储选择用docker compese在本地启动

纪律：
先tasking任务到 Stories.md中，包括  id,title,desc ,其中Desc描述中需要有具体的 AC(acceptance criteria) 列表
构建完成后编写 README.md，让我能根据说明逐步操作，按图索骥即可理解整个项目的工作原理。