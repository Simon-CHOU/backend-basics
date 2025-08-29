# Spring Boot 注解汇总

## 配置相关注解

注解名称|注解分类|注解功能
---|---|---
@ConfigurationProperties|配置相关|将配置文件中的属性值绑定到Java对象上
@EnableConfigurationProperties|配置相关|启用@ConfigurationProperties注解的配置类
@ImportResource|配置相关|导入XML配置文件
@PropertySource|配置相关|加载指定的配置文件
@Value|配置相关|注入配置文件中的值

## 依赖注入相关注解

注解名称|注解分类|注解功能
---|---|---
@Autowired|依赖注入|自动注入依赖的对象
@Bean|依赖注入|声明一个Bean对象
@Component|依赖注入|声明一个通用的Spring组件
@Controller|依赖注入|声明一个Spring MVC控制器
@Repository|依赖注入|声明一个数据访问层的组件
@Service|依赖注入|声明一个服务层的组件

## Web相关注解

注解名称|注解分类|注解功能
---|---|---
@CrossOrigin|Web相关|处理跨域请求
@DeleteMapping|Web相关|处理HTTP DELETE请求
@GetMapping|Web相关|处理HTTP GET请求
@PathVariable|Web相关|获取URL中的路径变量
@PostMapping|Web相关|处理HTTP POST请求
@PutMapping|Web相关|处理HTTP PUT请求
@RequestBody|Web相关|接收请求体中的JSON数据
@RequestMapping|Web相关|映射Web请求
@RequestParam|Web相关|获取请求参数
@ResponseBody|Web相关|将返回值序列化为JSON
@RestController|Web相关|组合@Controller和@ResponseBody的功能

## 条件注解

注解名称|注解分类|注解功能
---|---|---
@ConditionalOnBean|条件注解|当存在指定的Bean时条件成立
@ConditionalOnClass|条件注解|当存在指定的类时条件成立
@ConditionalOnMissingBean|条件注解|当不存在指定的Bean时条件成立
@ConditionalOnMissingClass|条件注解|当不存在指定的类时条件成立
@ConditionalOnProperty|条件注解|当配置属性满足条件时条件成立
@ConditionalOnWebApplication|条件注解|当应用是Web应用时条件成立

## 测试相关注解

注解名称|注解分类|注解功能
---|---|---
@AutoConfigureMockMvc|测试相关|自动配置MockMvc
@MockBean|测试相关|创建并注入一个Mock对象
@SpringBootTest|测试相关|Spring Boot测试注解
@TestConfiguration|测试相关|测试配置类
@WebMvcTest|测试相关|用于测试Spring MVC控制器

## 其他注解

注解名称|注解分类|注解功能
---|---|---
@EnableAutoConfiguration|其他|启用Spring Boot的自动配置机制
@Import|其他|导入其他配置类
@Scheduled|其他|设置定时任务
@SpringBootApplication|其他|Spring Boot应用程序的入口点，组合了@EnableAutoConfiguration、@ComponentScan和@Configuration
@Transactional|其他|声明事务范围和规则