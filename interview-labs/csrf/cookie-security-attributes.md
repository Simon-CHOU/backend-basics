# Cookie 安全属性详解

## 什么是 Cookie 安全属性？

Cookie 安全属性是 HTTP Cookie 规范中定义的一系列标志和参数，用于控制 Cookie 的行为和安全性。这些属性决定了 Cookie 何时被发送、如何被访问以及在什么条件下被传输，是 Web 应用安全防护的重要组成部分。

## Cookie 安全属性完整列表

### 1. HttpOnly 属性

**作用**：防止客户端脚本（JavaScript）访问 Cookie

```java
// Spring Boot 设置 HttpOnly
Cookie cookie = new Cookie("sessionId", "abc123");
cookie.setHttpOnly(true);
response.addCookie(cookie);

// 或者通过 ResponseCookie（Spring 5+）
ResponseCookie responseCookie = ResponseCookie.from("sessionId", "abc123")
    .httpOnly(true)
    .build();
response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
```

**HTTP 响应头示例**：
```http
Set-Cookie: sessionId=abc123; HttpOnly
```

**安全价值**：
- 防止 XSS 攻击窃取 Cookie
- 即使页面存在 XSS 漏洞，攻击者也无法通过 `document.cookie` 获取敏感 Cookie

**测试验证**：
```javascript
// 在浏览器控制台中测试
console.log(document.cookie); // HttpOnly Cookie 不会出现在结果中
```

### 2. Secure 属性

**作用**：确保 Cookie 仅通过 HTTPS 连接传输

```java
// Spring Boot 设置 Secure
Cookie cookie = new Cookie("sessionId", "abc123");
cookie.setSecure(true);
response.addCookie(cookie);

// 使用 ResponseCookie
ResponseCookie responseCookie = ResponseCookie.from("sessionId", "abc123")
    .secure(true)
    .build();
```

**HTTP 响应头示例**：
```http
Set-Cookie: sessionId=abc123; Secure
```

**安全价值**：
- 防止 Cookie 在不安全的 HTTP 连接中被窃听
- 强制使用加密传输

**注意事项**：
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                // 强制 HTTPS
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        return tomcat;
    }
}
```

### 3. SameSite 属性

**作用**：控制跨站请求时 Cookie 的发送行为，防止 CSRF 攻击

**三种取值**：
- **Strict**：最严格，仅同站请求发送
- **Lax**：默认值，大部分跨站请求不发送，但顶级导航（如链接点击）会发送
- **None**：所有跨站请求都发送（需要配合 Secure 使用）

```java
// Spring Boot 设置 SameSite
@Component
public class SameSiteCookieConfig {
    
    public void setStrictSameSite(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("sessionId", "abc123")
            .sameSite("Strict")
            .httpOnly(true)
            .secure(true)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    public void setLaxSameSite(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("csrfToken", "xyz789")
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    public void setNoneSameSite(HttpServletResponse response) {
        // None 必须配合 Secure 使用
        ResponseCookie cookie = ResponseCookie.from("trackingId", "track123")
            .sameSite("None")
            .secure(true)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
```

**HTTP 响应头示例**：
```http
Set-Cookie: sessionId=abc123; SameSite=Strict
Set-Cookie: csrfToken=xyz789; SameSite=Lax
Set-Cookie: trackingId=track123; SameSite=None; Secure
```

**SameSite 行为对比表**：

| 请求类型 | Strict | Lax | None |
|----------|--------|-----|------|
| 同站请求 | ✅ | ✅ | ✅ |
| 跨站表单提交 | ❌ | ❌ | ✅ |
| 跨站链接点击 | ❌ | ✅ | ✅ |
| 跨站 AJAX | ❌ | ❌ | ✅ |
| 跨站图片/iframe | ❌ | ❌ | ✅ |

### 4. Domain 属性

**作用**：指定 Cookie 的有效域名范围

```java
// 设置 Domain 属性
Cookie cookie = new Cookie("userPref", "theme=dark");
cookie.setDomain(".example.com"); // 包括所有子域名
response.addCookie(cookie);

// 使用 ResponseCookie
ResponseCookie responseCookie = ResponseCookie.from("userPref", "theme=dark")
    .domain(".example.com")
    .build();
```

**安全考虑**：
```java
@Service
public class CookieDomainValidator {
    
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
        "example.com", 
        ".example.com", 
        "api.example.com"
    );
    
    public boolean isValidDomain(String domain) {
        return ALLOWED_DOMAINS.contains(domain);
    }
    
    public void setSecureDomainCookie(String domain, HttpServletResponse response) {
        if (!isValidDomain(domain)) {
            throw new IllegalArgumentException("Invalid domain: " + domain);
        }
        
        ResponseCookie cookie = ResponseCookie.from("sessionId", generateSessionId())
            .domain(domain)
            .httpOnly(true)
            .secure(true)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
```

### 5. Path 属性

**作用**：限制 Cookie 的有效路径范围

```java
// 设置 Path 属性
Cookie cookie = new Cookie("adminToken", "admin123");
cookie.setPath("/admin"); // 仅在 /admin 路径下有效
response.addCookie(cookie);

// 使用 ResponseCookie
ResponseCookie responseCookie = ResponseCookie.from("adminToken", "admin123")
    .path("/admin")
    .httpOnly(true)
    .secure(true)
    .build();
```

**路径安全策略**：
```java
@Component
public class PathBasedCookieManager {
    
    public void setUserCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("userSession", "user123")
            .path("/user")
            .httpOnly(true)
            .maxAge(Duration.ofHours(2))
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    public void setAdminCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("adminSession", "admin123")
            .path("/admin")
            .httpOnly(true)
            .secure(true)
            .maxAge(Duration.ofMinutes(30)) // 更短的过期时间
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
```

### 6. Max-Age 和 Expires 属性

**作用**：控制 Cookie 的生命周期

```java
// 使用 Max-Age（推荐）
Cookie cookie = new Cookie("sessionId", "abc123");
cookie.setMaxAge(3600); // 1小时后过期
response.addCookie(cookie);

// 使用 ResponseCookie 设置精确时间
ResponseCookie responseCookie = ResponseCookie.from("sessionId", "abc123")
    .maxAge(Duration.ofHours(2))
    .build();

// 设置会话 Cookie（浏览器关闭时删除）
Cookie sessionCookie = new Cookie("tempData", "temp123");
sessionCookie.setMaxAge(-1); // 会话 Cookie
```

**生命周期管理策略**：
```java
@Service
public class CookieLifecycleManager {
    
    // 短期会话 Cookie
    public void setShortTermCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("shortSession", "short123")
            .maxAge(Duration.ofMinutes(15))
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    // 长期记住我 Cookie
    public void setRememberMeCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("rememberMe", "remember123")
            .maxAge(Duration.ofDays(30))
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    // 立即删除 Cookie
    public void deleteCookie(String cookieName, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
            .maxAge(Duration.ZERO)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
```

## 如何选择"适当的"Cookie安全属性？

### 1. 基于 Cookie 用途的选择矩阵

| Cookie 类型 | HttpOnly | Secure | SameSite | Max-Age | 说明 |
|-------------|----------|--------|----------|---------|------|
| 会话认证 | ✅ | ✅ | Strict | 短期 | 最高安全级别 |
| CSRF Token | ❌ | ✅ | Lax | 中期 | 需要 JS 访问 |
| 用户偏好 | ❌ | ✅ | Lax | 长期 | 用户体验优先 |
| 跟踪分析 | ✅ | ✅ | None | 长期 | 跨站功能需求 |
| 临时数据 | ✅ | ✅ | Strict | 会话 | 浏览器关闭删除 |

### 2. 安全级别分类

```java
@Component
public class CookieSecurityLevelManager {
    
    // 高安全级别（认证、授权相关）
    public ResponseCookie createHighSecurityCookie(String name, String value) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(Duration.ofMinutes(30))
            .path("/")
            .build();
    }
    
    // 中等安全级别（功能性 Cookie）
    public ResponseCookie createMediumSecurityCookie(String name, String value) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .maxAge(Duration.ofHours(2))
            .path("/")
            .build();
    }
    
    // 低安全级别（用户偏好、非敏感数据）
    public ResponseCookie createLowSecurityCookie(String name, String value) {
        return ResponseCookie.from(name, value)
            .httpOnly(false) // 允许 JS 访问
            .secure(true)
            .sameSite("Lax")
            .maxAge(Duration.ofDays(30))
            .path("/")
            .build();
    }
}
```

### 3. 环境相关的选择策略

```java
@Configuration
public class EnvironmentBasedCookieConfig {
    
    @Value("${app.environment:development}")
    private String environment;
    
    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;
    
    public ResponseCookie createEnvironmentAwareCookie(String name, String value) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
            .httpOnly(true)
            .path("/");
        
        // 生产环境强制使用更严格的安全设置
        if ("production".equals(environment)) {
            builder.secure(true)
                   .sameSite("Strict")
                   .maxAge(Duration.ofMinutes(15));
        } else if ("staging".equals(environment)) {
            builder.secure(sslEnabled)
                   .sameSite("Lax")
                   .maxAge(Duration.ofHours(1));
        } else {
            // 开发环境相对宽松
            builder.secure(false)
                   .sameSite("Lax")
                   .maxAge(Duration.ofHours(8));
        }
        
        return builder.build();
    }
}
```

## 怎么设置Cookie安全属性？

### 1. Spring Boot 中的多种设置方式

#### 方式一：传统 Cookie 对象
```java
@RestController
public class TraditionalCookieController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletResponse response) {
        Cookie sessionCookie = new Cookie("JSESSIONID", "session123");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(true);
        sessionCookie.setMaxAge(1800); // 30分钟
        sessionCookie.setPath("/");
        
        response.addCookie(sessionCookie);
        return ResponseEntity.ok("Login successful");
    }
}
```

#### 方式二：ResponseCookie（推荐）
```java
@RestController
public class ModernCookieController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("sessionId", "session123")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(Duration.ofMinutes(30))
            .path("/")
            .domain(".example.com")
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("Login successful");
    }
}
```

#### 方式三：全局配置
```java
@Configuration
public class GlobalCookieConfig {
    
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        serializer.setHttpOnly(true);
        serializer.setSecure(true);
        serializer.setSameSite("Strict");
        serializer.setUseSecureCookie(true);
        return serializer;
    }
    
    @Bean
    public TomcatContextCustomizer sameSiteContextCustomizer() {
        return context -> {
            final Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
            cookieProcessor.setSameSiteCookies("Strict");
            context.setCookieProcessor(cookieProcessor);
        };
    }
}
```

### 2. 配置文件方式

#### application.yml 配置
```yaml
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: true
        same-site: strict
        max-age: 1800
        name: JSESSIONID
        path: /
        domain: .example.com

spring:
  session:
    redis:
      flush-mode: on_save
      namespace: spring:session
    cookie:
      http-only: true
      secure: true
      same-site: strict
      max-age: 1800
```

#### application.properties 配置
```properties
# Session Cookie 配置
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
server.servlet.session.cookie.max-age=1800
server.servlet.session.cookie.name=JSESSIONID
server.servlet.session.cookie.path=/
server.servlet.session.cookie.domain=.example.com

# Spring Session 配置
spring.session.cookie.http-only=true
spring.session.cookie.secure=true
spring.session.cookie.same-site=strict
spring.session.cookie.max-age=1800
```

### 3. 统一的 Cookie 管理服务

```java
@Service
public class CookieManagementService {
    
    @Value("${app.cookie.secure:true}")
    private boolean defaultSecure;
    
    @Value("${app.cookie.same-site:Strict}")
    private String defaultSameSite;
    
    @Value("${app.cookie.domain:.example.com}")
    private String defaultDomain;
    
    public void setAuthenticationCookie(String sessionId, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("AUTH_SESSION", sessionId)
            .httpOnly(true)
            .secure(defaultSecure)
            .sameSite(defaultSameSite)
            .maxAge(Duration.ofMinutes(30))
            .path("/")
            .domain(defaultDomain)
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        logCookieCreation("AUTH_SESSION", "Authentication cookie set");
    }
    
    public void setCsrfTokenCookie(String csrfToken, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("CSRF_TOKEN", csrfToken)
            .httpOnly(false) // CSRF Token 需要被 JavaScript 访问
            .secure(defaultSecure)
            .sameSite("Lax") // CSRF Token 使用 Lax 模式
            .maxAge(Duration.ofHours(1))
            .path("/")
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        logCookieCreation("CSRF_TOKEN", "CSRF token cookie set");
    }
    
    public void setUserPreferenceCookie(String preferences, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("USER_PREF", preferences)
            .httpOnly(false)
            .secure(defaultSecure)
            .sameSite("Lax")
            .maxAge(Duration.ofDays(365)) // 长期保存
            .path("/")
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        logCookieCreation("USER_PREF", "User preference cookie set");
    }
    
    public void clearCookie(String cookieName, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
            .maxAge(Duration.ZERO)
            .path("/")
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        logCookieCreation(cookieName, "Cookie cleared");
    }
    
    private void logCookieCreation(String cookieName, String action) {
        log.info("Cookie operation: {} - {}", cookieName, action);
    }
}
```

### 4. Cookie 安全属性验证

```java
@Component
public class CookieSecurityValidator {
    
    public void validateCookieSecurity(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                validateIndividualCookie(cookie, request);
            }
        }
    }
    
    private void validateIndividualCookie(Cookie cookie, HttpServletRequest request) {
        String cookieName = cookie.getName();
        
        // 验证敏感 Cookie 的安全属性
        if (isSensitiveCookie(cookieName)) {
            if (!cookie.isHttpOnly()) {
                log.warn("Sensitive cookie {} is not HttpOnly", cookieName);
            }
            
            if (!request.isSecure() && cookie.getSecure()) {
                log.warn("Secure cookie {} sent over non-HTTPS connection", cookieName);
            }
        }
        
        // 验证 Cookie 值的安全性
        if (containsSuspiciousContent(cookie.getValue())) {
            log.warn("Cookie {} contains suspicious content", cookieName);
        }
    }
    
    private boolean isSensitiveCookie(String cookieName) {
        return cookieName.contains("SESSION") || 
               cookieName.contains("AUTH") || 
               cookieName.contains("TOKEN");
    }
    
    private boolean containsSuspiciousContent(String value) {
        String[] suspiciousPatterns = {"<script", "javascript:", "data:", "vbscript:"};
        String lowerValue = value.toLowerCase();
        return Arrays.stream(suspiciousPatterns)
                .anyMatch(lowerValue::contains);
    }
}
```

## 最佳实践总结

### 1. 安全属性设置检查清单

- [ ] **HttpOnly**: 敏感 Cookie 必须设置
- [ ] **Secure**: 生产环境必须设置
- [ ] **SameSite**: 根据用途选择 Strict/Lax/None
- [ ] **Max-Age**: 设置合理的过期时间
- [ ] **Path**: 限制 Cookie 的有效路径
- [ ] **Domain**: 谨慎设置域名范围

### 2. 不同场景的推荐配置

```java
// 会话认证 Cookie（最高安全级别）
ResponseCookie.from("SESSION_ID", sessionId)
    .httpOnly(true)
    .secure(true)
    .sameSite("Strict")
    .maxAge(Duration.ofMinutes(30))
    .path("/")
    .build();

// CSRF Token Cookie
ResponseCookie.from("CSRF_TOKEN", csrfToken)
    .httpOnly(false) // 需要 JS 访问
    .secure(true)
    .sameSite("Lax")
    .maxAge(Duration.ofHours(1))
    .path("/")
    .build();

// 用户偏好 Cookie
ResponseCookie.from("USER_PREFERENCES", preferences)
    .httpOnly(false)
    .secure(true)
    .sameSite("Lax")
    .maxAge(Duration.ofDays(365))
    .path("/")
    .build();
```

### 3. 监控和审计

```java
@Component
public class CookieSecurityAuditor {
    
    @EventListener
    public void auditCookieCreation(CookieCreationEvent event) {
        log.info("Cookie created: name={}, secure={}, httpOnly={}, sameSite={}", 
                event.getName(), event.isSecure(), event.isHttpOnly(), event.getSameSite());
    }
    
    @Scheduled(fixedRate = 3600000) // 每小时检查
    public void auditCookieCompliance() {
        // 检查当前活跃的 Cookie 是否符合安全标准
        log.info("Performing cookie security compliance audit");
    }
}
```

通过合理设置这些 Cookie 安全属性，可以显著提高 Web 应用的安全性，防范 XSS、CSRF、会话劫持等常见攻击。