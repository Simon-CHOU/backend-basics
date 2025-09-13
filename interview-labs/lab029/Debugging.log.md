# Lab029 调试问题记录

本文档记录了在lab029项目开发过程中遇到的调试问题及其解决方案，作为案例研究的补充材料。

## 问题1：Redis Testcontainers 依赖配置错误

### 问题描述
- **文件位置**: `user-session-be/pom.xml` 第93-97行
- **错误信息**: 找不到 `org.testcontainers.redis` 依赖项
- **根本原因**: 
  1. 缺少testcontainers版本管理配置
  2. 错误的artifactId（redis应为testcontainers-redis）
  3. 错误的groupId和版本号

### 解决方案
1. **添加版本管理**:
   ```xml
   <properties>
       <testcontainers.version>1.19.3</testcontainers.version>
   </properties>
   ```

2. **修正依赖配置**:
   ```xml
   <!-- 核心testcontainers模块 -->
   <dependency>
       <groupId>org.testcontainers</groupId>
       <artifactId>testcontainers</artifactId>
       <version>${testcontainers.version}</version>
       <scope>test</scope>
   </dependency>
   
   <!-- JUnit集成 -->
   <dependency>
       <groupId>org.testcontainers</groupId>
       <artifactId>junit-jupiter</artifactId>
       <version>${testcontainers.version}</version>
       <scope>test</scope>
   </dependency>
   
   <!-- Redis模块 -->
   <dependency>
       <groupId>com.redis.testcontainers</groupId>
       <artifactId>testcontainers-redis</artifactId>
       <version>1.6.4</version>
       <scope>test</scope>
   </dependency>
   ```

### 验证结果
- ✅ `mvn dependency:resolve` 执行成功
- ✅ 所有依赖正确解析
- ✅ 可在集成测试中使用Redis Testcontainers

---

## 问题2：AccessInterceptor 编译错误

### 问题描述
- **文件位置**: `AccessInterceptor.java` 第80行和第103行
- **错误信息**: 
  - 第80行：找不到 `UserUtil.setCurrentUser()` 方法
  - 第103行：找不到 `UserUtil.clearContext()` 方法
- **根本原因**: UserUtil类中缺少这两个方法的定义

### 解决方案
在 `UserUtil.java` 中添加别名方法：

```java
/**
 * 设置当前用户信息 (别名方法)
 */
public static void setCurrentUser(UserInfo userInfo) {
    setUserInfo(userInfo);
}

/**
 * 清除用户上下文 (别名方法)
 */
public static void clearContext() {
    clearUserInfo();
}
```

### 验证结果
- ✅ 编译成功：`mvn compile` 返回 BUILD SUCCESS
- ✅ 测试通过：`mvn test` 返回 BUILD SUCCESS

---

## 问题3：UserController 编译错误

### 问题描述
- **文件位置**: `UserController.java` 多个位置
- **错误信息**: 
  - 第40行：找不到 `UserUtil.getCurrentUser()` 方法
  - 第128行：找不到 `UserUtil.getCurrentUserPermissions()` 方法
  - 第129行：找不到 `UserUtil.getCurrentUserRoles()` 方法
- **根本原因**: UserUtil类中缺少这些便捷方法的定义

### 解决方案
在 `UserUtil.java` 中添加扩展的便捷方法：

```java
/**
 * 获取当前用户信息 (别名方法)
 */
public static UserInfo getCurrentUser() {
    return getUserInfo();
}

/**
 * 获取当前用户权限列表 (别名方法)
 */
public static List<String> getCurrentUserPermissions() {
    return getUserPermissions();
}

/**
 * 获取当前用户角色列表 (别名方法)
 */
public static List<String> getCurrentUserRoles() {
    return getUserRoles();
}

/**
 * 获取当前用户ID (别名方法)
 */
public static String getCurrentUserId() {
    return getUserId();
}

/**
 * 获取当前用户名 (别名方法)
 */
public static String getCurrentUsername() {
    return getUsername();
}

/**
 * 获取当前用户显示名称 (别名方法)
 */
public static String getCurrentUserDisplayName() {
    return getDisplayName();
}

/**
 * 获取当前用户邮箱
 */
public static String getCurrentUserEmail() {
    UserInfo userInfo = getUserInfo();
    return userInfo != null ? userInfo.getEmail() : null;
}

/**
 * 获取当前用户部门
 */
public static String getCurrentUserDepartment() {
    UserInfo userInfo = getUserInfo();
    return userInfo != null ? userInfo.getDepartment() : null;
}
```

### 验证结果
- ✅ 编译成功：`mvn compile` 返回 BUILD SUCCESS
- ✅ 测试通过：`mvn test` 返回 BUILD SUCCESS

---

## 总结

### 解决问题的关键原则
1. **系统性分析**: 从编译错误信息入手，逐步定位根本原因
2. **别名方法模式**: 通过提供别名方法保持API兼容性
3. **渐进式验证**: 每次修改后立即验证，确保问题得到解决
4. **完整性检查**: 确保所有相关的方法都得到补充

### 最佳实践
1. **依赖管理**: 使用properties统一管理版本号
2. **API设计**: 提供多种调用方式以适应不同的使用场景
3. **文档完整**: 为所有新增方法提供完整的JavaDoc文档
4. **测试驱动**: 通过编译和测试验证修复效果

### 技术债务预防
1. 建立完善的API文档和使用规范
2. 在代码审查中关注方法命名的一致性
3. 定期检查和更新依赖配置
4. 建立自动化测试覆盖关键功能

---

**记录时间**: 2025-09-13  
**解决状态**: 全部问题已解决  
**项目状态**: 编译和测试均通过