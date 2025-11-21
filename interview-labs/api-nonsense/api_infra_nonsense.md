# 基于 Spring Boot 2.x 的 API 安全基础设施全攻略（Auth/IAM/Crypt/CORS/CSRF/防刷）——For Dummies 版

> 适用读者：完全不懂安全也能读懂。以最小知识颗粒度讲清楚所有关键概念、原理、边界与复现方法；示例以 Java 21 + Spring Boot 2.x 为基准。


## 1. 开卷先列“关键概念与拓扑排序”（先学什么，再学什么）

- 身份认证 Authentication：确认“你是谁”。
- 授权 Authorization：确认“你能做什么”。
- 身份与访问管理 IAM：在组织维度管理账号、角色、权限、策略与审计。
- 凭证 Token（如 JWT）：把认证结果装进可验证的字符串，供后续请求携带。
- 会话 Session：服务器维护的登录状态；与 JWT 的“自包含状态”相对。
- 加密/摘要 Crypt：保护与校验数据，包括密码哈希、消息签名、对称与非对称加密。
- 同源策略 SOP：浏览器默认安全底座，限制跨站脚本能访问的资源范围。
- CORS：在 SOP 之上，给“跨源请求”开白名单，决定哪些域能访问你的 API。
- CSRF：跨站请求伪造，诱导用户浏览器在“已登录”状态下发危险请求。
- XSS：跨站脚本，向页面注入恶意脚本冒充用户操作；与 CSRF不同攻击层面。
- 防刷/限流 Rate Limiting：限制单位时间内的请求次数/并发，避免滥用与DoS。
- 零信任与最小权限：默认不信任何请求，权限按最小集合授予。
- 风险控制 Risk Control：在登录、支付等高风险操作做额外校验（如验证码、二次验证）。

学习顺序（拓扑排序）：
1) 同源策略→CORS→CSRF/XSS 这条“浏览器端约束”线。先懂底层再懂例外。
2) 身份认证→授权→IAM→Token/Session 这条“服务端身份与权限”线。
3) Crypt（哈希/签名/加密）作为认证与防篡改的技术基座。
4) 防刷/限流作为外层防线，保护整体系统资源与安全策略的可执行性。


## 2. 定义边界与负向排除（避免混淆）

- Authentication ≠ Authorization：认证只回答“你是谁”，授权回答“你能做什么”。
- JWT ≠ 加密：JWT 本身通常是签名而非加密；敏感数据不应放进未加密的 JWT。
- CORS ≠ 安全防护：CORS 是“谁能调用”白名单，不拦截恶意服务器端调用。
- CSRF ≠ XSS：CSRF利用用户已登录的浏览器；XSS是在页面内执行恶意脚本。
- 防刷 ≠ 防攻击全能：限流只能限制速率，不能识别业务逻辑欺诈或数据入侵。
- Session ≠ JWT：Session 由服务端保存状态；JWT 是客户端自包含凭证，服务端无状态。

核心差异与适用场景：
- Session 更适合传统网页与同域应用；JWT 更适合移动端/多端/跨网关的微服务。
- HS256（对称签名）适合单服务；RS256（非对称签名）适合多服务与密钥分发。
- CSRF 防护仅当浏览器自动携带 Cookie 才需要；若用 Authorization 头携带 JWT，通常禁用 CSRF。
- 防刷实现：应用内限流适合简易场景；Redis/网关层限流适合分布式与高并发。

反事实推理（锁定本质）：
- 若缺少“可验证的身份绑定”，授权决策失效→认证是权限之母。
- 若没有“不可抵赖的签名”，任意人能伪造令牌→签名是令牌可信的根。
- 若浏览器不默认携带 Cookie，CSRF 攻击就难成立→CSRF 依赖浏览器自动携带状态。
- 若不做防刷，任何安全策略都可能在资源耗尽前来不及执行→限流是可用性的底线。


## 3. 总体架构与数据流（For Dummies 图解）

```
-----------+        +-------------------+        +------------------+        +------------------+
| Browser   |  CORS  | API Gateway/Nginx |  JWT   | Spring Boot 2.x  |  RBAC | Database (IAM)   |
| Mobile    |  ----> |   Rate Limit      |  ----> | Security Filter  |  -->  | users/roles/perms |
| Other     |        |   CSRF (cookie)   |        | Controllers      |       | audit/logs        |
-----------+        +-------------------+        +------------------+        +------------------+
                          | Redis
                          | limit buckets
```

请求生命线（简化）：
1) 客户端发起跨域请求，先过 CORS 白名单。
2) 网关或应用内做限流，阻断超速刷与并发过载。
3) 认证：登录颁发 JWT；后续请求携带 `Authorization: Bearer ...`。
4) 授权：按用户-角色-权限策略判定是否允许访问。
5) 业务执行，审计记录与风控监控。


## 4. 设计决策（Best Practices）

- 使用无状态 JWT 做 API 身份传递；在服务端统一验证签名与过期。
- 采用 RBAC（角色-权限）+ 可能的 ABAC（属性规则）实现精细授权与审计。
- 密码存储使用 BCrypt；令牌采用 RS256（长远看更利于分发与密钥轮换）。
- 对浏览器场景：若使用 Cookie 会话，启用 CSRF Token；若使用 JWT 头，通常禁用 CSRF。
- CORS 明确域白名单、允许方法与头；拒绝 `*` 在生产环境。
- 防刷放在靠前位置（网关或过滤器），优先采用 Redis 滑动窗口限流。
- 安全配置用“最小权限原则”，默认全部拒绝，仅对白名单路径放开。
- 密钥管理：密钥不入代码库，使用环境变量与版本化轮换策略。


## 5. IAM 模型与数据库

最小表集合：
- `users(id, username, password_hash, status)`
- `roles(id, code, name)`
- `permissions(id, code, name)`
- `user_roles(user_id, role_id)`
- `role_permissions(role_id, permission_id)`

授权判断：用户通过角色映射权限；审计可记录 `user_id, action, resource, timestamp, result`。

示例 SQL（建表草案）：
```
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) UNIQUE NOT NULL,
  password_hash VARCHAR(200) NOT NULL,
  status VARCHAR(16) NOT NULL
);
CREATE TABLE roles (
  id BIGINT PRIMARY KEY,
  code VARCHAR(64) UNIQUE NOT NULL,
  name VARCHAR(128) NOT NULL
);
CREATE TABLE permissions (
  id BIGINT PRIMARY KEY,
  code VARCHAR(64) UNIQUE NOT NULL,
  name VARCHAR(128) NOT NULL
);
CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
);
CREATE TABLE role_permissions (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id)
);
```


## 6. Crypt（哈希/签名/加密）

- 密码哈希：`BCrypt`，带盐与成本因子，提高抗暴力破解能力。
- 令牌签名：`RS256`（非对称），便于多服务校验与密钥轮换；或 `HS256`（对称）适合单体。
- 敏感数据加密：必要时使用 AES-GCM 做字段级加密，不放进 JWT。
- 随机性：使用 `SecureRandom`，避免弱随机源。

Maven 依赖提示（示例）：
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
  <groupId>com.nimbusds</groupId>
  <artifactId>nimbus-jose-jwt</artifactId>
  <version>9.37</version>
</dependency>
```


## 7. CORS（跨源资源共享）

- 白名单域：仅允许可信前端域名。
- 允许方法：GET/POST/PUT/DELETE 等。
- 允许头：`Authorization` 等业务必需头。
- 暴露头：必要时暴露 `Location` 等。
- 凭证：如需 Cookie，`allowCredentials=true` 并避免 `*`。


## 8. CSRF（跨站请求伪造）

- 使用 Cookie 会话的 Web 表单：开启 CSRF Token（如 `CookieCsrfTokenRepository`）。
- 使用无状态 JWT 的纯 API：通常禁用 CSRF，并要求使用 `Authorization` 头携带令牌。
- 高风险操作：二次确认、验证码或一次性操作令牌。


## 9. 防刷/限流（滑动窗口 + Redis）

- 维度：按 IP、用户ID、端点、策略分层限流。
- 策略：滑动窗口优于固定窗口，平滑且公平。
- 存储：Redis 以 Lua 脚本原子计数；或网关层 Bucket4j。
- 响应：标准化 429 状态码与可读的错误体。

Docker Compose（Redis）：
```
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: ["redis-server", "--save", "", "--appendonly", "no"]
```


## 10. 5-Why 深度递归（到第一性原理）

- 为什么需要认证？因为系统需区分主体身份。
- 为什么区分身份？因为资源访问需可控与可审计。
- 为什么要授权？因为不同主体权限边界不同。
- 为什么要签名与哈希？因为需要防伪造、防篡改与抗破解。
- 为什么要限流与CORS/CSRF？因为需确保安全策略在有限资源下可执行。

递归到底层：
- 数学底层：签名基于不可逆函数与数论难题（RSA 椭圆曲线）；哈希基于抗碰撞与抗原像性质。
- 物理定律：资源有限性决定必须做限流与防滥用，否则任何策略形同虚设。
- 代码实现层：安全策略最终落实为过滤器、拦截器、密钥校验与数据库约束。


## 11. Spring Boot 2.x 安全配置与代码示例（Java 21）

### 11.1 Security 配置（无状态 JWT + CORS + 可选 CSRF）

```
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.authorizeHttpRequests(auth -> auth
      .requestMatchers("/auth/login", "/auth/refresh").permitAll()
      .anyRequest().authenticated());
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
    return http.build();
  }
  @Bean
  PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.example.com"));
    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE"));
    config.setAllowedHeaders(List.of("Authorization","Content-Type"));
    config.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
```

### 11.2 JWT 过滤器与服务

```
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService uds) {
    this.jwtService = jwtService;
    this.userDetailsService = uds;
  }
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      String username = jwtService.extractUsername(token);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails ud = userDetailsService.loadUserByUsername(username);
        if (jwtService.isTokenValid(token, ud)) {
          UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      }
    }
    chain.doFilter(request, response);
  }
}
```

```
@Service
public class JwtService {
  private final RSAKey rsaKey;
  public JwtService(@Value("${security.jwt.private}") String privatePem, @Value("${security.jwt.public}") String publicPem) {
    this.rsaKey = KeyLoader.loadRsaKey(privatePem, publicPem);
  }
  public String generateToken(String username, Collection<? extends GrantedAuthority> authorities, Duration ttl) {
    JWSSigner signer = new RSASSASigner(rsaKey.toPrivateKey());
    JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build();
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
      .subject(username)
      .claim("roles", authorities.stream().map(GrantedAuthority::getAuthority).toList())
      .issueTime(new Date())
      .expirationTime(Date.from(Instant.now().plus(ttl)))
      .build();
    SignedJWT jwt = new SignedJWT(header, claims);
    try { jwt.sign(signer); } catch (JOSEException e) { throw new RuntimeException(e); }
    return jwt.serialize();
  }
  public String extractUsername(String token) {
    try { return SignedJWT.parse(token).getJWTClaimsSet().getSubject(); } catch (ParseException e) { return null; }
  }
  public boolean isTokenValid(String token, UserDetails ud) {
    try {
      SignedJWT jwt = SignedJWT.parse(token);
      JWSVerifier verifier = new RSASSAVerifier(rsaKey.toPublicKey());
      boolean sigOk = jwt.verify(verifier);
      Date exp = jwt.getJWTClaimsSet().getExpirationTime();
      return sigOk && exp != null && exp.after(new Date()) && ud.getUsername().equals(jwt.getJWTClaimsSet().getSubject());
    } catch (Exception e) { return false; }
  }
}
```

```
public final class KeyLoader {
  public static RSAKey loadRsaKey(String privatePem, String publicPem) {
    try {
      PEMParser privParser = new PEMParser(new StringReader(privatePem));
      Object privObj = privParser.readObject();
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
      PrivateKey priv = converter.getPrivateKey((PrivateKeyInfo) privObj);
      PEMParser pubParser = new PEMParser(new StringReader(publicPem));
      SubjectPublicKeyInfo pubInfo = (SubjectPublicKeyInfo) pubParser.readObject();
      PublicKey pub = converter.getPublicKey(pubInfo);
      return new RSAKey.Builder((RSAPublicKey) pub).privateKey((RSAPrivateKey) priv).build();
    } catch (Exception e) { throw new RuntimeException(e); }
  }
}
```

提示：若不使用 PEM 解析，可改用 JKS/PKCS12；或改 HS256，减少密钥管理复杂度。

### 11.3 登录与刷新接口

```
@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserDetailsService uds;
  public AuthController(AuthenticationManager am, JwtService js, UserDetailsService uds) { this.authenticationManager = am; this.jwtService = js; this.uds = uds; }
  @PostMapping("/login")
  public TokenResponse login(@RequestBody LoginRequest req) {
    Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
    UserDetails ud = uds.loadUserByUsername(auth.getName());
    String token = jwtService.generateToken(ud.getUsername(), ud.getAuthorities(), Duration.ofHours(2));
    return new TokenResponse(token);
  }
  @PostMapping("/refresh")
  public TokenResponse refresh(@AuthenticationPrincipal UserDetails ud) {
    String token = jwtService.generateToken(ud.getUsername(), ud.getAuthorities(), Duration.ofHours(2));
    return new TokenResponse(token);
  }
}
record LoginRequest(String username, String password) {}
record TokenResponse(String token) {}
```

### 11.4 用户与密码编码器

```
@Service
public class SimpleUserDetailsService implements UserDetailsService {
  private final JdbcTemplate jdbc;
  public SimpleUserDetailsService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Map<String,Object> u = jdbc.queryForMap("select id, username, password_hash from users where username=?", username);
    List<GrantedAuthority> auths = jdbc.query("select p.code from permissions p join role_permissions rp on p.id=rp.permission_id join user_roles ur on ur.role_id=rp.role_id where ur.user_id=?", rs -> {
      List<GrantedAuthority> list = new ArrayList<>();
      while (rs.next()) list.add(new SimpleGrantedAuthority(rs.getString(1)));
      return list;
    }, u.get("id"));
    return new User(username, (String)u.get("password_hash"), auths);
  }
}
```

注册时保存密码：
```
@Service
public class RegisterService {
  private final PasswordEncoder encoder;
  private final JdbcTemplate jdbc;
  public RegisterService(PasswordEncoder encoder, JdbcTemplate jdbc) { this.encoder = encoder; this.jdbc = jdbc; }
  public void register(String username, String rawPassword) {
    String hash = encoder.encode(rawPassword);
    jdbc.update("insert into users(username, password_hash, status) values(?,?,?)", username, hash, "ACTIVE");
  }
}
```

### 11.5 Redis 滑动窗口限流过滤器（429）

```
@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final StringRedisTemplate redis;
  public RateLimitFilter(StringRedisTemplate redis) { this.redis = redis; }
  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {
    String key = "rl:" + Optional.ofNullable(req.getHeader("X-User-Id")).orElse(req.getRemoteAddr()) + ":" + req.getRequestURI();
    long now = System.currentTimeMillis();
    String script = "local k=KEYS[1] local now=ARGV[1] local win=ARGV[2] local max=ARGV[3] redis.call('ZREMRANGEBYSCORE',k,0,now-win) redis.call('ZADD',k,now,now) local c=redis.call('ZCARD',k) redis.call('PEXPIRE',k,win) if c>tonumber(max) then return 0 else return 1 end";
    boolean allow = Boolean.TRUE.equals(redis.execute((RedisCallback<Boolean>) connection -> {
      return connection.scriptingCommands().eval(script.getBytes(StandardCharsets.UTF_8), ReturnType.BOOLEAN, 1, key.getBytes(StandardCharsets.UTF_8), String.valueOf(now).getBytes(), String.valueOf(10000).getBytes(), String.valueOf(50).getBytes());
    }));
    if (!allow) { resp.setStatus(429); resp.getWriter().write("Too Many Requests"); return; }
    chain.doFilter(req, resp);
  }
}
```

在 `SecurityFilterChain` 中把 `RateLimitFilter` 放在靠前位置（如 `addFilterBefore(jwtFilter, ...)` 前另加）。


## 12. SOP（可复现指南）

- 依赖准备：JDK 21；Maven；Docker Desktop；`docker compose up -d` 启动 Redis。
- 添加依赖：Spring Security、Web、Data Redis、Nimbus JWT。
- 配置密钥：通过环境变量注入 `security.jwt.private` 与 `security.jwt.public`，不写入代码库。
- 启动应用：`mvn spring-boot:run`。
- 登录获取令牌：PowerShell 使用 `Invoke-RestMethod -Method Post -Uri http://localhost:8080/auth/login -Body '{"username":"u","password":"p"}' -ContentType 'application/json'`。
- 携带令牌访问：在请求头加入 `Authorization: Bearer <token>`。
- 跨域验证：前端域名访问 API，检查预检请求与 CORS 响应头。
- 限流验证：并发或循环调用同一端点，观察 429 响应。


## 13. 风险控制与运营守则

- 登录与支付等高风险操作引入验证码与二次验证。
- 令牌短时效与刷新令牌分离，服务端可维护刷新令牌黑名单。
- 审计日志与告警：异常频次、失败认证、限流触发计数。
- 密钥轮换：多版本并行校验，平滑替换。
- 数据最小化：令牌内只放必要声明，敏感数据留在服务端。


## 14. 常见误区与纠偏

- 误以为 CORS 能防止服务器端攻击；纠偏：它只影响浏览器跨域行为。
- 误以为禁用 CSRF 就安全；纠偏：仅在使用 JWT 头、无 Cookie 场景下才宜禁用。
- 误以为 JWT 必须加密；纠偏：JWT 常用签名，若需保密另行加密负载。
- 误以为限流等同防爬；纠偏：限流只是速率控制，需配合行为分析与风控。


## 15. 结论（金字塔顶）

- 先有可验证身份与最小权限，再谈功能安全。
- 用签名与哈希构建不可伪造的信任，再用限流守住资源边界。
- CORS/CSRF 针对浏览器态的约束，JWT/Session 针对服务端态的身份；分层清晰，边界分明。


## 16. 参考实现可选项（扩展）

- 使用 RS256 + JWK 集合发布公钥，便于多服务校验。
- 提供 Idempotency-Key 头避免重复提交。
- 使用系统级 WAF 与 API Gateway 提前过滤恶意流量。

## 17. 金融级 SonarQube 合规指南（基于 Rule 的落地）

### 17.1 概念与边界（For Dummies）

- Bug：会导致错误行为的缺陷。
- Code Smell：不会立即崩溃，但可维护性差的设计味道。
- Vulnerability：可被攻击者利用的安全缺陷，需立即修复或阻断上线。
- Security Hotspot：涉及安全敏感点但需人工复核上下文来判定是否漏洞。
- Rule：Sonar 的检测条目，用于识别上述问题的具体模式。
- Quality Profile：规则集的选择与定制，决定扫描的“题库”。
- Quality Gate：上线闸门，设定通过阈值（零新漏洞、覆盖率阈值等）。

边界定义：
- Vulnerability ≠ Hotspot：前者是确定漏洞，后者需人工复核，不可混同。若把 Hotspot 当漏洞会造成误报；若把漏洞当 Hotspot 会放过风险。
- 覆盖率 ≠ 安全：高覆盖率不保证无漏洞，但低覆盖率会削弱静态与动态检测效果。

反事实：
- 若没有 Quality Gate，即使扫描出高风险也可能被合并发布→质量门是“最后防线”。
- 若规则集不包含 OWASP Top 10，常见注入类问题将漏检→Profile 的完备性决定检出率。

### 17.2 流程与架构

```
Dev -> Unit Test/IT -> Jacoco Coverage -> Sonar Scan -> Quality Gate
                                     |                         |
                                     v                         v
                                 Report HTML               Block Merge
```

推荐：本地与 CI 均执行 Sonar 扫描，合并请求必须通过 Quality Gate。

### 17.3 使用 Docker Compose 启动 SonarQube（含 Postgres）

```
services:
  sonarqube:
    image: sonarqube:10-community
    ports:
      - "9000:9000"
    environment:
      - SONAR_JDBC_URL=jdbc:postgresql://db:5432/sonar
      - SONAR_JDBC_USERNAME=sonar
      - SONAR_JDBC_PASSWORD=sonar
    depends_on:
      - db
  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_USER=sonar
      - POSTGRES_PASSWORD=sonar
      - POSTGRES_DB=sonar
    ports:
      - "5433:5432"
```

PowerShell 启动：`docker compose up -d`，浏览器访问 `http://localhost:9000`，首次登录创建 Token。

### 17.4 项目与构建配置（Maven + Jacoco + Sonar）

`pom.xml` 添加：
```
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <outputDirectory>${project.build.directory}/jacoco</outputDirectory>
  </configuration>
</plugin>
<plugin>
  <groupId>org.sonarsource.scanner.maven</groupId>
  <artifactId>sonar-maven-plugin</artifactId>
  <version>3.10.0.2594</version>
</plugin>
```

`sonar-project.properties`（若使用 CLI/多模块通用）：
```
sonar.projectKey=api-nonsense
sonar.projectName=api-nonsense
sonar.sourceEncoding=UTF-8
sonar.language=java
sonar.java.source=21
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.junit.reportPaths=target/surefire-reports
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.exclusions=**/generated/**,**/*Config.java
sonar.host.url=http://localhost:9000
```

本地扫描命令：
- 运行测试并生成覆盖率：`mvn clean verify`
- 扫描并上报：`mvn sonar:sonar -Dsonar.login=<your_token> -Dsonar.host.url=http://localhost:9000`

### 17.5 规则集建议（金融级）

- 启用 OWASP Top 10 相关安全规则（注入、身份认证、敏感数据暴露、访问控制、日志与监控）。
- 强制加密规则：禁止弱算法（MD5、SHA-1）、禁止不安全随机数、禁止硬编码密钥。
- 网络与IO规则：避免路径遍历、命令注入、禁止信任所有证书的 SSL 连接。
- 代码质量规则：空指针风险、资源泄漏、异常吞吃、未关闭流、过度可变共享状态。
- 日志规则：禁止将凭证、令牌、隐私数据写入日志；使用结构化日志与掩码处理。

典型修复模式（示例）：

1) SQL 注入：
```
var sql = "select * from users where username = ?";
try (var conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
  ps.setString(1, username);
  try (var rs = ps.executeQuery()) { /* ... */ }
}
```

2) XSS：后端不回显未经处理的用户输入；必要时对文本做 HTML 转义；前端使用安全模板。

3) 路径遍历：
```
Path base = Paths.get("/safe/base").toAbsolutePath().normalize();
Path target = base.resolve(userInput).normalize();
if (!target.startsWith(base)) throw new IllegalArgumentException();
```

4) 不安全 SSL：
```
var httpClient = HttpClient.newBuilder()
  .sslParameters(new SSLParameters())
  .build();
```
不要使用全信任 TrustManager；仅加载受信 CA。

5) 异常与资源：统一异常处理，不吞吃异常；所有 IO 使用 try-with-resources；线程池有界。

### 17.6 常见失败项与整改清单

- 硬编码凭证/密钥：改为环境变量或密钥管理系统注入。
- 弱加密：替换为 BCrypt、AES-GCM、RS256；禁用 MD5/SHA-1。
- 不安全反序列化：禁用默认反序列化，使用白名单或 JSON 映射。
- 未验证输入：对外部输入施加长度、格式与白名单校验。
- 广义 `catch (Exception)`：缩窄异常类型并记录上下文。
- 日志泄露：令牌、密码、身份证号进行脱敏与禁止输出。
- 覆盖率过低：为安全关键路径补充单元与集成测试，目标新代码覆盖率≥90%。

### 17.7 SOP（端到端复现，确保通过质量门）

- 启动 SonarQube：`docker compose up -d`，创建项目与生成 Token。
- 在项目根执行：`mvn clean verify` 生成 `target/site/jacoco/jacoco.xml`。
- 执行扫描：`mvn sonar:sonar -Dsonar.login=<token> -Dsonar.host.url=http://localhost:9000`。
- 在 Sonar UI：关联 Quality Profile（含 OWASP 规则），设置 Quality Gate：
  - New Code：`Vulnerabilities=0`、`Security Hotspots Reviewed=100%`、`Coverage>=90%`、`Duplications<=3%`。
  - Overall：`Vulnerabilities=0`、`Coverage>=80%`。
- 修复报告的漏洞与热点，重复执行扫描直至通过 Quality Gate。

### 17.8 5-Why（到第一性原理）

- 为什么要 Sonar？早期静态分析能以低成本发现高风险缺陷。
- 为什么要规则与门？让安全成为“可量化门槛”，可被审计与度量。
- 为什么要覆盖率？测试执行路径越多，越能暴露隐藏缺陷。
- 为什么要热点复核？安全情境复杂，需人工确认避免误报与漏报。
- 为什么要禁止弱算法与硬编码？因为数学与工程上已被证明易被破解或泄露。

### 17.9 审计与交付物（金融场景）

- Sonar 报告截图与导出，含质量门通过记录。
- Quality Profile 与 Quality Gate 配置快照与变更记录。
- 漏洞与热点整改单，含代码差异与复测结果。
- 密钥管理与日志脱敏方案说明。

### 17.10 边界与负向排除

- Sonar ≠ 渗透测试：它是静态分析，仍需配合 SAST/DAST 与红队演练。
- 零告警 ≠ 零风险：需持续监控与代码评审、依赖安全更新。
- 忽略/抑制问题 ≠ 修复：除非有严谨的风险评估与豁免流程，不允许使用忽略标签。

## 18. Crypto 三处落地实践（HTTP载荷、落库加密、日志脱敏）

### 18.1 先总后分与边界

- HTTP 请求体加解密：在 TLS 之外做应用层保密与认证，适用于端到端保密或网关终止 TLS 的场景。
- 落库前加密：字段级加密与密钥轮换，保证静态数据泄露时不可读。
- 日志加密与脱敏：对凭证与隐私数据进行掩码或加密，满足合规与最小暴露。

边界与负向排除：
- TLS ≠ 应用层加密：TLS保护链路，但终止点之后明文仍可被读取；应用层加密可实现真正端到端。
- 加密 ≠ 哈希：哈希不可逆，适合密码；加密可逆，适合业务数据保密。
- 脱敏 ≠ 删除：脱敏是可控显示，不等价于彻底不记录；敏感数据应尽量不进入日志。

反事实：
- 若仅依赖 TLS 而网关终止后进入明文，内部侧被入侵即可读敏感数据→需应用层加密。
- 未做字段加密，数据库被拖库即明文外泄→需 AES-GCM 等。
- 日志未脱敏，审计或故障排查即泄露隐私→需掩码或加密。

### 18.2 HTTP 请求体加解密（混合加密：RSA/ECDH + AES-GCM）

握手与数据流：
```
Client -> /crypto/key -> {kid, alg, pubKey}
Client --encrypt JSON with AES-GCM, wrap AES by RSA/ECDH--> POST /api
Server --unwrap key--> decrypt body --> Controller
```

设计要点：
- 暴露公钥获取端点，包含 `kid` 与失效时间，支持密钥轮换。
- 客户端用公钥包裹一次性 AES-GCM 密钥，负载使用 AES-GCM 加密（含认证标签）。
- 服务端过滤器在控制器前解密，恢复原始 JSON。

示例代码（Java 21）：
```
@RestController
@RequestMapping("/crypto")
public class CryptoController {
  private final CryptoKeyService cryptoKeyService;
  public CryptoController(CryptoKeyService s) { this.cryptoKeyService = s; }
  @GetMapping("/key")
  public Map<String,Object> key() {
    return cryptoKeyService.currentPublicKey();
  }
}
```

```
@Service
public class CryptoKeyService {
  private final KeyPair keyPair;
  private final String kid;
  public CryptoKeyService() {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      this.keyPair = kpg.generateKeyPair();
      this.kid = UUID.randomUUID().toString();
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  public Map<String,Object> currentPublicKey() {
    String pub = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    return Map.of("kid", kid, "alg", "RSA-OAEP", "pubKey", pub, "expiresAt", Instant.now().plus(Duration.ofHours(12)).toString());
  }
  public PrivateKey privateKey() { return keyPair.getPrivate(); }
}
```

```
@Component
public class EncryptedJsonFilter extends OncePerRequestFilter {
  private final CryptoKeyService keyService;
  public EncryptedJsonFilter(CryptoKeyService ks) { this.keyService = ks; }
  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {
    if ("1".equals(req.getHeader("X-Encrypted")) && "application/json".equalsIgnoreCase(req.getContentType())) {
      String body = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      Map<String,Object> env = new ObjectMapper().readValue(body, new TypeReference<Map<String,Object>>(){});
      byte[] ek = Base64.getDecoder().decode((String) env.get("ek"));
      byte[] iv = Base64.getDecoder().decode((String) env.get("iv"));
      byte[] ct = Base64.getDecoder().decode((String) env.get("ct"));
      try {
        Cipher unwrap = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        unwrap.init(Cipher.UNWRAP_MODE, keyService.privateKey());
        Key aesKey = unwrap.unwrap(ek, "AES", Cipher.SECRET_KEY);
        Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        aes.init(Cipher.DECRYPT_MODE, aesKey, spec);
        byte[] plain = aes.doFinal(ct);
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(req) {
          @Override
          public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(plain);
            return new ServletInputStream() {
              @Override public boolean isFinished() { return bais.available()==0; }
              @Override public boolean isReady() { return true; }
              @Override public void setReadListener(ReadListener readListener) {}
              @Override public int read() { return bais.read(); }
            };
          }
          @Override public String getHeader(String name) { return "Content-Type".equalsIgnoreCase(name)?"application/json":super.getHeader(name); }
          @Override public String getContentType() { return "application/json"; }
        };
        chain.doFilter(wrapper, resp);
        return;
      } catch (Exception e) { resp.setStatus(400); return; }
    }
    chain.doFilter(req, resp);
  }
}
```

SOP：
- 暴露 `/crypto/key`，前端请求前获取公钥与 `kid`。
- 前端用混合加密构造包，设置 `X-Encrypted: 1` 与 `Content-Type: application/json`。
- 在安全链路中把 `EncryptedJsonFilter` 放在控制器前。

### 18.3 业务落库前加密（AES-GCM + JPA Converter + 轮换）

设计要点：
- 字段级透明加密，支持密钥版本 `kv` 与轮换策略。
- 存储格式包括 `kv:iv:ciphertext`，Base64 编码。
- 查询能力：避免在密文上做模糊搜索；等值查询用哈希索引替代。

示例代码：
```
@Converter
public class AesGcmStringConverter implements AttributeConverter<String, String> {
  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (attribute == null) return null;
    try {
      String kv = KeyVault.currentVersion();
      SecretKey key = KeyVault.currentKey();
      byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
      byte[] ct = c.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
      return kv+":"+Base64.getEncoder().encodeToString(iv)+":"+Base64.getEncoder().encodeToString(ct);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    try {
      String[] parts = dbData.split(":");
      String kv = parts[0];
      SecretKey key = KeyVault.keyByVersion(kv);
      byte[] iv = Base64.getDecoder().decode(parts[1]);
      byte[] ct = Base64.getDecoder().decode(parts[2]);
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
      byte[] pt = c.doFinal(ct);
      return new String(pt, StandardCharsets.UTF_8);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
}
```

```
public final class KeyVault {
  private static final Map<String, SecretKey> KEYS = new ConcurrentHashMap<>();
  private static volatile String CURRENT = "v1";
  static {
    KEYS.put("v1", new SecretKeySpec(Base64.getDecoder().decode(System.getenv("DATA_KEY_V1")), "AES"));
  }
  public static String currentVersion() { return CURRENT; }
  public static SecretKey currentKey() { return KEYS.get(CURRENT); }
  public static SecretKey keyByVersion(String v) { return KEYS.get(v); }
}
```

SOP：
- 通过环境变量注入数据密钥，避免入库。
- 为包含隐私的字段添加 `@Convert(converter = AesGcmStringConverter.class)`。
- 轮换策略：新密钥设为 `CURRENT`，增量读旧密文写回新密文。

### 18.4 日志中的加密与脱敏（Masking + 最小化）

设计要点：
- 日志最小化原则：不记录凭证与隐私；必须记录时做掩码或加密。
- 统一入口：所有用户输入进入日志前通过掩码器处理。
- 正则掩码：处理常见字段如 `password`、`token`、`authorization`、身份证号与手机号。

示例代码：
```
public final class Masking {
  private static final Pattern PWD = Pattern.compile("\"password\"\s*:\s*\".*?\"", Pattern.CASE_INSENSITIVE);
  private static final Pattern TOKEN = Pattern.compile("\"token\"\s*:\s*\"[A-Za-z0-9._-]+\"", Pattern.CASE_INSENSITIVE);
  private static final Pattern AUTHZ = Pattern.compile("authorization:\s*bearer\s+[A-Za-z0-9._-]+", Pattern.CASE_INSENSITIVE);
  private static final Pattern ID = Pattern.compile("\b\d{15,18}\b");
  private static final Pattern PHONE = Pattern.compile("\b1\d{10}\b");
  public static String format(String s) {
    if (s == null) return null;
    String r = PWD.matcher(s).replaceAll("\"password\":\"***\"");
    r = TOKEN.matcher(r).replaceAll("\"token\":\"***\"");
    r = AUTHZ.matcher(r).replaceAll("authorization: bearer ***");
    r = ID.matcher(r).replaceAll("***");
    r = PHONE.matcher(r).replaceAll("***");
    return r;
  }
}
```

```
@RestController
public class SampleController {
  private static final Logger log = LoggerFactory.getLogger(SampleController.class);
  @PostMapping("/sample")
  public Map<String,Object> sample(@RequestBody Map<String,Object> body) {
    String raw = new ObjectMapper().valueToTree(body).toString();
    log.info(Masking.format(raw));
    return Map.of("ok", true);
  }
}
```

SOP：
- 在日志切面或拦截器中统一调用 `Masking.format`。
- 对异常与审计日志进行分类，敏感字段始终掩码。
- 对需要密文留痕的场景，使用公钥加密后再记录。

### 18.5 5-Why 与第一性原理

- 为什么要应用层加密？因为终止 TLS 后仍需保密与来源认证。
- 为什么要字段级加密？因为静态数据是被拖库的首要目标。
- 为什么要日志脱敏？因为日志会广泛分发与保留，泄露面大。
- 为什么选 AES-GCM？因为同时提供保密与完整性认证。
- 为什么要密钥轮换？因为密钥泄露与老化不可避免，动态轮换降低风险。