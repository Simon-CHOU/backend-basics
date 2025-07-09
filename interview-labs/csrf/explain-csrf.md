# 基于Spring Boot RESTful API的CSRF攻击原理与防范

## 1. CSRF攻击原理深度解析

### 1.1 什么是CSRF攻击

CSRF（Cross-Site Request Forgery，跨站请求伪造）是一种恶意攻击，攻击者诱导用户在已认证的网站上执行非预期的操作。<mcreference link="https://developer.mozilla.org/en-US/docs/Web/Security/Attacks/CSRF" index="0">0</mcreference> 在前后端分离的架构中，这种攻击尤其需要重视，因为API接口通常依赖于认证令牌来验证用户身份。

### 1.2 攻击机制详解

#### HTTP协议的无状态特性
由于HTTP是无状态协议，Web应用通常使用以下方式维持用户会话：<mcreference link="https://www.geeksforgeeks.org/computer-networks/what-is-cross-site-request-forgery-csrf/" index="1">1</mcreference>
- Session Cookie
- JWT Token（存储在Cookie或LocalStorage中）
- Authorization Header中的Bearer Token

#### 浏览器的自动行为
浏览器在发送请求时会自动携带以下信息：
- 目标域名下的所有Cookie
- 基本认证信息
- 客户端证书

这种自动行为为CSRF攻击提供了可能性。

### 1.3 Spring Boot RESTful API中的CSRF攻击场景

#### 场景一：基于Cookie的Session认证
```java
// 用户登录后，服务器设置Session Cookie
@PostMapping("/api/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    // 验证用户凭据
    User user = authService.authenticate(request.getUsername(), request.getPassword());
    
    // 创建Session
    HttpSession session = httpServletRequest.getSession();
    session.setAttribute("user", user);
    
    // 设置Session Cookie（自动携带）
    return ResponseEntity.ok(new LoginResponse("success"));
}

// 敏感操作API
@PostMapping("/api/transfer")
public ResponseEntity<?> transferMoney(@RequestBody TransferRequest request, HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return ResponseEntity.status(401).build();
    }
    
    // 执行转账操作
    transferService.transfer(user.getId(), request.getToAccount(), request.getAmount());
    return ResponseEntity.ok("Transfer successful");
}
```

#### 攻击实施过程
1. **用户正常登录**：用户在 `https://bank.example.com` 登录，浏览器存储Session Cookie
2. **访问恶意网站**：用户访问攻击者控制的 `https://evil.com`
3. **恶意请求构造**：
```html
<!-- 恶意网站的HTML代码 -->
<form id="maliciousForm" action="https://bank.example.com/api/transfer" method="POST">
    <input type="hidden" name="toAccount" value="attacker-account" />
    <input type="hidden" name="amount" value="10000" />
</form>

<script>
// 页面加载时自动提交表单
document.getElementById('maliciousForm').submit();
</script>
```

4. **攻击成功**：浏览器自动携带bank.example.com的Session Cookie，服务器认为这是合法请求

#### 场景二：基于JWT Token的认证
```java
@PostMapping("/api/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    User user = authService.authenticate(request.getUsername(), request.getPassword());
    
    // 生成JWT Token
    String token = jwtService.generateToken(user);
    
    // 将Token存储在HttpOnly Cookie中
    Cookie cookie = new Cookie("jwt-token", token);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    response.addCookie(cookie);
    
    return ResponseEntity.ok(new LoginResponse("success"));
}

@PostMapping("/api/sensitive-operation")
public ResponseEntity<?> sensitiveOperation(@RequestBody OperationRequest request, 
                                           @CookieValue("jwt-token") String token) {
    if (!jwtService.validateToken(token)) {
        return ResponseEntity.status(401).build();
    }
    
    // 执行敏感操作
    return ResponseEntity.ok("Operation successful");
}
```

## 2. CSRF防范最佳实践

### 2.1 CSRF Token机制

#### 实现原理
CSRF Token是服务器生成的不可预测的随机值，嵌入到页面中，每次状态改变请求都必须携带正确的Token。<mcreference link="https://developer.mozilla.org/en-US/docs/Web/Security/Attacks/CSRF" index="0">0</mcreference>

#### Spring Security实现
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}
```

#### 前端Token处理
```javascript
// 获取CSRF Token
function getCsrfToken() {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    return { token, header };
}

// API请求时携带Token
function makeApiRequest(url, data) {
    const { token, header } = getCsrfToken();
    
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify(data)
    });
}
```

### 2.2 SameSite Cookie属性

```java
@Bean
public CookieSameSiteSupplier cookieSameSiteSupplier() {
    return CookieSameSiteSupplier.ofStrict();
}

// 或者在应用配置中
server:
  servlet:
    session:
      cookie:
        same-site: strict
```

**SameSite属性值说明：**
- `Strict`：最严格，完全禁止跨站请求携带Cookie
- `Lax`：相对宽松，允许安全的跨站请求（如GET链接）
- `None`：允许所有跨站请求，但必须配合Secure属性

### 2.3 双重Cookie验证

```java
@Component
public class DoubleCookieCSRFFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        if (isStateChangingRequest(httpRequest)) {
            String cookieToken = getCookieValue(httpRequest, "csrf-token");
            String headerToken = httpRequest.getHeader("X-CSRF-Token");
            
            if (!Objects.equals(cookieToken, headerToken)) {
                ((HttpServletResponse) response).setStatus(403);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isStateChangingRequest(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method) || 
               "DELETE".equals(method) || "PATCH".equals(method);
    }
}
```

### 2.4 Origin和Referer头验证

```java
@Component
public class OriginValidationFilter implements Filter {
    
    private final Set<String> allowedOrigins = Set.of(
        "https://app.example.com",
        "https://admin.example.com"
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        if (isStateChangingRequest(httpRequest)) {
            String origin = httpRequest.getHeader("Origin");
            String referer = httpRequest.getHeader("Referer");
            
            if (!isValidOrigin(origin) && !isValidReferer(referer)) {
                ((HttpServletResponse) response).setStatus(403);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isValidOrigin(String origin) {
        return origin != null && allowedOrigins.contains(origin);
    }
    
    private boolean isValidReferer(String referer) {
        if (referer == null) return false;
        return allowedOrigins.stream().anyMatch(referer::startsWith);
    }
}
```

### 2.5 自定义请求头验证

```java
// 要求所有API请求携带自定义头
@Component
public class CustomHeaderCSRFFilter implements Filter {
    
    private static final String REQUIRED_HEADER = "X-Requested-With";
    private static final String EXPECTED_VALUE = "XMLHttpRequest";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        if (isApiRequest(httpRequest)) {
            String headerValue = httpRequest.getHeader(REQUIRED_HEADER);
            
            if (!EXPECTED_VALUE.equals(headerValue)) {
                ((HttpServletResponse) response).setStatus(403);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }
}
```

## 3. 前后端分离架构的特殊考虑

### 3.1 JWT Token存储策略

#### 方案对比
| 存储方式 | 安全性 | CSRF风险 | XSS风险 | 推荐度 |
|---------|--------|----------|---------|--------|
| LocalStorage | 低 | 无 | 高 | ❌ |
| SessionStorage | 低 | 无 | 高 | ❌ |
| HttpOnly Cookie | 高 | 有 | 无 | ✅ |
| Memory + Refresh | 高 | 无 | 无 | ✅ |

#### 推荐实现：HttpOnly Cookie + CSRF Token
```java
@PostMapping("/api/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, 
                              HttpServletResponse response) {
    User user = authService.authenticate(request.getUsername(), request.getPassword());
    
    // 生成访问令牌（短期）
    String accessToken = jwtService.generateAccessToken(user);
    // 生成刷新令牌（长期）
    String refreshToken = jwtService.generateRefreshToken(user);
    
    // 访问令牌存储在HttpOnly Cookie中
    Cookie accessCookie = new Cookie("access-token", accessToken);
    accessCookie.setHttpOnly(true);
    accessCookie.setSecure(true);
    accessCookie.setMaxAge(15 * 60); // 15分钟
    accessCookie.setSameSite(Cookie.SameSite.STRICT.attributeValue());
    
    // 刷新令牌存储在HttpOnly Cookie中
    Cookie refreshCookie = new Cookie("refresh-token", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(true);
    refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7天
    refreshCookie.setSameSite(Cookie.SameSite.STRICT.attributeValue());
    
    response.addCookie(accessCookie);
    response.addCookie(refreshCookie);
    
    return ResponseEntity.ok(new LoginResponse("success"));
}
```

### 3.2 API设计最佳实践

#### 状态改变操作使用POST/PUT/DELETE
```java
// ❌ 错误：使用GET进行状态改变
@GetMapping("/api/user/{id}/delete")
public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok("User deleted");
}

// ✅ 正确：使用DELETE进行状态改变
@DeleteMapping("/api/user/{id}")
public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok("User deleted");
}
```

#### 敏感操作添加额外验证
```java
@PostMapping("/api/user/change-password")
public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                       @CookieValue("access-token") String token) {
    User user = jwtService.getUserFromToken(token);
    
    // 额外验证：要求提供当前密码
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        return ResponseEntity.badRequest().body("Current password is incorrect");
    }
    
    // 执行密码更改
    userService.changePassword(user.getId(), request.getNewPassword());
    
    return ResponseEntity.ok("Password changed successfully");
}
```

## 4. 完整的防护方案示例

### 4.1 Spring Boot配置

```java
@Configuration
@EnableWebSecurity
public class CSRFSecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 启用CSRF保护
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/public/**") // 公开API不需要CSRF保护
            )
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 会话管理
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            // 安全头
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("https://*.example.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### 4.2 前端集成示例

```javascript
// API客户端封装
class ApiClient {
    constructor() {
        this.baseURL = 'https://api.example.com';
        this.csrfToken = null;
        this.initCSRF();
    }
    
    async initCSRF() {
        try {
            const response = await fetch(`${this.baseURL}/api/csrf-token`, {
                credentials: 'include'
            });
            const data = await response.json();
            this.csrfToken = data.token;
        } catch (error) {
            console.error('Failed to initialize CSRF token:', error);
        }
    }
    
    async request(url, options = {}) {
        const defaultOptions = {
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        };
        
        // 为状态改变请求添加CSRF Token
        if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(options.method?.toUpperCase())) {
            if (this.csrfToken) {
                defaultOptions.headers['X-CSRF-TOKEN'] = this.csrfToken;
            }
        }
        
        const mergedOptions = {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...options.headers
            }
        };
        
        const response = await fetch(`${this.baseURL}${url}`, mergedOptions);
        
        // 处理CSRF Token过期
        if (response.status === 403) {
            await this.initCSRF();
            // 重试请求
            mergedOptions.headers['X-CSRF-TOKEN'] = this.csrfToken;
            return fetch(`${this.baseURL}${url}`, mergedOptions);
        }
        
        return response;
    }
}

// 使用示例
const apiClient = new ApiClient();

// 安全的API调用
async function transferMoney(toAccount, amount) {
    try {
        const response = await apiClient.request('/api/transfer', {
            method: 'POST',
            body: JSON.stringify({ toAccount, amount })
        });
        
        if (response.ok) {
            const result = await response.json();
            console.log('Transfer successful:', result);
        } else {
            console.error('Transfer failed:', response.statusText);
        }
    } catch (error) {
        console.error('Network error:', error);
    }
}
```

## 5. 监控和检测

### 5.1 CSRF攻击检测

```java
@Component
public class CSRFAttackDetector {
    
    private final Logger logger = LoggerFactory.getLogger(CSRFAttackDetector.class);
    
    @EventListener
    public void handleCSRFViolation(CSRFViolationEvent event) {
        // 记录攻击尝试
        logger.warn("Potential CSRF attack detected: IP={}, User-Agent={}, Referer={}",
            event.getClientIP(),
            event.getUserAgent(),
            event.getReferer());
        
        // 发送告警
        alertService.sendSecurityAlert("CSRF Attack Detected", event);
        
        // 可选：临时封禁IP
        if (isRepeatedAttack(event.getClientIP())) {
            ipBlockingService.blockIP(event.getClientIP(), Duration.ofHours(1));
        }
    }
}
```

### 5.2 安全指标监控

```java
@Component
public class SecurityMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter csrfViolations;
    private final Counter suspiciousRequests;
    
    public SecurityMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.csrfViolations = Counter.builder("security.csrf.violations")
            .description("Number of CSRF violations detected")
            .register(meterRegistry);
        this.suspiciousRequests = Counter.builder("security.requests.suspicious")
            .description("Number of suspicious requests")
            .register(meterRegistry);
    }
    
    public void recordCSRFViolation() {
        csrfViolations.increment();
    }
    
    public void recordSuspiciousRequest(String reason) {
        suspiciousRequests.increment(Tags.of("reason", reason));
    }
}
```

## 6. 总结

在前后端分离的Spring Boot RESTful API架构中，CSRF防护需要综合考虑多个方面：

### 6.1 核心防护策略
1. **CSRF Token机制**：最可靠的防护方法
2. **SameSite Cookie**：现代浏览器的有效防护
3. **Origin/Referer验证**：额外的验证层
4. **自定义请求头**：简单有效的API保护

### 6.2 架构设计原则
1. **最小权限原则**：只给必要的权限
2. **纵深防御**：多层安全控制
3. **安全默认**：默认配置应该是安全的
4. **持续监控**：实时检测和响应

### 6.3 开发最佳实践
1. 所有状态改变操作使用POST/PUT/DELETE方法
2. 敏感操作添加额外验证步骤
3. 合理设置Cookie属性（HttpOnly、Secure、SameSite）
4. 实施完整的日志记录和监控
5. 定期进行安全测试和评估

通过实施这些防护措施，可以有效防范CSRF攻击，保护用户数据和系统安全。