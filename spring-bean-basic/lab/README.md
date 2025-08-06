# Spring Bean 深度理解实验室

## 📚 知识点拓扑分析

基于知乎回答的核心观点，我们将Spring Bean的知识体系分解为以下层次结构：

```
                    ┌─────────────────────────────────┐
                    │         应用层                   │
                    │  ┌─────────────────────────────┐ │
                    │  │ 自定义Starter │ 观察者模式  │ │
                    │  └─────────────────────────────┘ │
                    └─────────────────────────────────┘
                    ┌─────────────────────────────────┐
                    │         增强层                   │
                    │  ┌─────────────────────────────┐ │
                    │  │   AOP   │   循环依赖解决    │ │
                    │  └─────────────────────────────┘ │
                    └─────────────────────────────────┘
                    ┌─────────────────────────────────┐
                    │         使用层                   │
                    │  ┌─────────────────────────────┐ │
                    │  │      依赖注入 (DI)          │ │
                    │  └─────────────────────────────┘ │
                    └─────────────────────────────────┘
                    ┌─────────────────────────────────┐
                    │       生命周期层                 │
                    │  ┌─────────────────────────────┐ │
                    │  │前置处理器│实例化│后置处理器 │ │
                    │  └─────────────────────────────┘ │
                    └─────────────────────────────────┘
                    ┌─────────────────────────────────┐
                    │         基础层                   │
                    │  ┌─────────────────────────────┐ │
                    │  │   Spring容器 = HashMap      │ │
                    │  └─────────────────────────────┘ │
                    └─────────────────────────────────┘
```

## 🎯 核心概念解构

### 1. 基础概念
- **Spring容器**: 本质是一个HashMap，key是Bean名称，value是Bean实例
- **Bean**: 就是放在容器中的对象实例，与手动new出来的对象本质相同

### 2. 生命周期概念
- **前置处理器**: 准备实例化类的条件（BeanFactoryPostProcessor）
- **后置处理器**: 实例化后的处理逻辑（BeanPostProcessor）

### 3. 核心功能
- **依赖注入**: 从HashMap中根据name取出实例使用
- **AOP**: 在方法执行的关键时间点添加回调函数
- **循环依赖**: 通过三级缓存解决A依赖B，B依赖A的问题

## 🚀 实验室路线图

我们将通过7个渐进式实验，从零开始构建对Spring Bean的完整理解：

```
Lab 1: 容器本质     →  Lab 2: 生命周期    →  Lab 3: 依赖注入基础
   ↓                      ↓                      ↓
Lab 4: 注解驱动     →  Lab 5: AOP实现     →  Lab 6: 循环依赖
   ↓                      ↓                      ↓
Lab 7: Spring Boot集成 → 完整理解Spring Bean生态
```

---

## 🧪 Lab 1: 理解容器本质 - 手动实现HashMap容器

### 目标
理解"Spring容器就是个HashMap"这个核心概念

### 理论基础
Spring容器的本质就是一个存储Bean的HashMap，其中：
- Key: Bean的名称（String）
- Value: Bean的实例（Object）

### 实现步骤

#### 1.1 创建简单容器类
```java
// src/main/java/lab1/SimpleContainer.java
package lab1;

import java.util.HashMap;
import java.util.Map;

public class SimpleContainer {
    // 这就是Spring容器的本质 - 一个HashMap
    private Map<String, Object> beanMap = new HashMap<>();
    
    // 注册Bean到容器
    public void registerBean(String name, Object bean) {
        beanMap.put(name, bean);
        System.out.println("Bean注册成功: " + name + " -> " + bean.getClass().getSimpleName());
    }
    
    // 从容器获取Bean
    public Object getBean(String name) {
        Object bean = beanMap.get(name);
        if (bean == null) {
            throw new RuntimeException("Bean not found: " + name);
        }
        return bean;
    }
    
    // 检查Bean是否存在
    public boolean containsBean(String name) {
        return beanMap.containsKey(name);
    }
    
    // 获取所有Bean名称
    public String[] getBeanNames() {
        return beanMap.keySet().toArray(new String[0]);
    }
    
    // 显示容器内容（用于调试）
    public void showContainer() {
        System.out.println("=== 容器内容 ===");
        beanMap.forEach((name, bean) -> 
            System.out.println(name + " -> " + bean.getClass().getSimpleName() + "@" + bean.hashCode())
        );
        System.out.println("===============");
    }
}
```

#### 1.2 创建测试用的Bean类
```java
// src/main/java/lab1/UserService.java
package lab1;

public class UserService {
    private String serviceName = "UserService";
    
    public void createUser(String username) {
        System.out.println(serviceName + ": 创建用户 " + username);
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
```

```java
// src/main/java/lab1/OrderService.java
package lab1;

public class OrderService {
    private String serviceName = "OrderService";
    
    public void createOrder(String orderId) {
        System.out.println(serviceName + ": 创建订单 " + orderId);
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
```

#### 1.3 创建测试类
```java
// src/main/java/lab1/Lab1Test.java
package lab1;

public class Lab1Test {
    public static void main(String[] args) {
        System.out.println("=== Lab 1: 理解容器本质 ===");
        
        // 1. 创建我们的简单容器
        SimpleContainer container = new SimpleContainer();
        
        // 2. 手动创建Bean实例（相当于new操作）
        UserService userService = new UserService();
        OrderService orderService = new OrderService();
        
        // 3. 将Bean注册到容器中
        container.registerBean("userService", userService);
        container.registerBean("orderService", orderService);
        
        // 4. 显示容器内容
        container.showContainer();
        
        // 5. 从容器获取Bean并使用
        UserService retrievedUserService = (UserService) container.getBean("userService");
        OrderService retrievedOrderService = (OrderService) container.getBean("orderService");
        
        // 6. 验证获取的Bean就是我们放入的Bean
        System.out.println("原始userService == 获取的userService: " + (userService == retrievedUserService));
        System.out.println("原始orderService == 获取的orderService: " + (orderService == retrievedOrderService));
        
        // 7. 使用Bean
        retrievedUserService.createUser("张三");
        retrievedOrderService.createOrder("ORDER-001");
        
        // 8. 尝试获取不存在的Bean
        try {
            container.getBean("nonExistentBean");
        } catch (RuntimeException e) {
            System.out.println("预期的异常: " + e.getMessage());
        }
    }
}
```

### 🔍 调试步骤

1. **设置断点**：在`SimpleContainer.registerBean()`方法的第一行设置断点
2. **启动调试**：运行`Lab1Test.main()`方法
3. **观察变量**：
   - 查看`beanMap`的内容变化
   - 观察`name`和`bean`参数的值
4. **单步执行**：逐行执行，观察HashMap的put操作
5. **继续调试**：在`getBean()`方法设置断点，观察get操作

### ✅ 验证结果

运行后应该看到：
```
=== Lab 1: 理解容器本质 ===
Bean注册成功: userService -> UserService
Bean注册成功: orderService -> OrderService
=== 容器内容 ===
userService -> UserService@12345678
orderService -> OrderService@87654321
===============
原始userService == 获取的userService: true
原始orderService == 获取的orderService: true
UserService: 创建用户 张三
OrderService: 创建订单 ORDER-001
预期的异常: Bean not found: nonExistentBean
```

### 💡 关键理解点

1. **容器本质**：Spring容器确实就是一个HashMap
2. **Bean存储**：Bean以name-instance的键值对形式存储
3. **引用一致性**：从容器获取的Bean就是原来放入的那个实例
4. **单例特性**：同一个name对应同一个实例

---

## 🧪 Lab 2: Bean生命周期 - 实现前置/后置处理器

### 目标
理解Bean的创建过程和生命周期管理

### 理论基础
Bean的生命周期包括：
1. **前置处理器**：准备实例化的条件
2. **实例化**：创建Bean实例
3. **后置处理器**：对实例化后的Bean进行处理

### 实现步骤

#### 2.1 定义处理器接口
```java
// src/main/java/lab2/BeanPostProcessor.java
package lab2;

public interface BeanPostProcessor {
    // Bean初始化前的处理
    Object postProcessBeforeInitialization(Object bean, String beanName);
    
    // Bean初始化后的处理
    Object postProcessAfterInitialization(Object bean, String beanName);
}
```

#### 2.2 增强版容器
```java
// src/main/java/lab2/AdvancedContainer.java
package lab2;

import java.util.*;

public class AdvancedContainer {
    private Map<String, Object> beanMap = new HashMap<>();
    private Map<String, Class<?>> beanDefinitions = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    
    // 注册Bean定义（类信息）
    public void registerBeanDefinition(String name, Class<?> beanClass) {
        beanDefinitions.put(name, beanClass);
        System.out.println("Bean定义注册: " + name + " -> " + beanClass.getSimpleName());
    }
    
    // 添加Bean后置处理器
    public void addBeanPostProcessor(BeanPostProcessor processor) {
        beanPostProcessors.add(processor);
        System.out.println("添加后置处理器: " + processor.getClass().getSimpleName());
    }
    
    // 获取Bean（懒加载）
    public Object getBean(String name) {
        // 如果Bean已经存在，直接返回
        if (beanMap.containsKey(name)) {
            return beanMap.get(name);
        }
        
        // 如果Bean不存在，则创建Bean
        return createBean(name);
    }
    
    // 创建Bean的完整生命周期
    private Object createBean(String name) {
        Class<?> beanClass = beanDefinitions.get(name);
        if (beanClass == null) {
            throw new RuntimeException("Bean definition not found: " + name);
        }
        
        System.out.println("\n=== 开始创建Bean: " + name + " ===");
        
        try {
            // 1. 实例化Bean
            System.out.println("1. 实例化Bean...");
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            System.out.println("   实例化完成: " + bean.getClass().getSimpleName() + "@" + bean.hashCode());
            
            // 2. 前置处理器处理
            System.out.println("2. 执行前置处理器...");
            for (BeanPostProcessor processor : beanPostProcessors) {
                bean = processor.postProcessBeforeInitialization(bean, name);
            }
            
            // 3. 初始化Bean（这里简化，实际Spring会调用init方法）
            System.out.println("3. 初始化Bean...");
            initializeBean(bean, name);
            
            // 4. 后置处理器处理
            System.out.println("4. 执行后置处理器...");
            for (BeanPostProcessor processor : beanPostProcessors) {
                bean = processor.postProcessAfterInitialization(bean, name);
            }
            
            // 5. 将Bean放入容器
            System.out.println("5. Bean放入容器");
            beanMap.put(name, bean);
            
            System.out.println("=== Bean创建完成: " + name + " ===\n");
            return bean;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + name, e);
        }
    }
    
    // 初始化Bean（可以在这里调用init方法）
    private void initializeBean(Object bean, String name) {
        System.out.println("   初始化Bean: " + name);
        // 这里可以调用Bean的初始化方法
    }
    
    // 显示容器状态
    public void showContainer() {
        System.out.println("=== 容器状态 ===");
        System.out.println("Bean定义数量: " + beanDefinitions.size());
        System.out.println("Bean实例数量: " + beanMap.size());
        System.out.println("后置处理器数量: " + beanPostProcessors.size());
        
        System.out.println("\nBean定义:");
        beanDefinitions.forEach((name, clazz) -> 
            System.out.println("  " + name + " -> " + clazz.getSimpleName())
        );
        
        System.out.println("\nBean实例:");
        beanMap.forEach((name, bean) -> 
            System.out.println("  " + name + " -> " + bean.getClass().getSimpleName() + "@" + bean.hashCode())
        );
        System.out.println("===============\n");
    }
}
```

#### 2.3 创建具体的后置处理器
```java
// src/main/java/lab2/LoggingBeanPostProcessor.java
package lab2;

public class LoggingBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("   [前置处理器] 处理Bean: " + beanName + 
                          " (类型: " + bean.getClass().getSimpleName() + ")");
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("   [后置处理器] 处理Bean: " + beanName + 
                          " (类型: " + bean.getClass().getSimpleName() + ")");
        return bean;
    }
}
```

```java
// src/main/java/lab2/PropertyInjectBeanPostProcessor.java
package lab2;

import java.lang.reflect.Field;

public class PropertyInjectBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 在初始化前注入一些属性
        try {
            if (bean.getClass().getSimpleName().contains("Service")) {
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getName().equals("version")) {
                        field.setAccessible(true);
                        field.set(bean, "1.0.0");
                        System.out.println("   [属性注入] 为" + beanName + "注入version=1.0.0");
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
```

#### 2.4 创建测试Bean
```java
// src/main/java/lab2/UserService.java
package lab2;

public class UserService {
    private String serviceName = "UserService";
    private String version;
    
    public void createUser(String username) {
        System.out.println(serviceName + " v" + version + ": 创建用户 " + username);
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getVersion() {
        return version;
    }
}
```

#### 2.5 测试类
```java
// src/main/java/lab2/Lab2Test.java
package lab2;

public class Lab2Test {
    public static void main(String[] args) {
        System.out.println("=== Lab 2: Bean生命周期管理 ===\n");
        
        // 1. 创建增强版容器
        AdvancedContainer container = new AdvancedContainer();
        
        // 2. 添加后置处理器
        container.addBeanPostProcessor(new LoggingBeanPostProcessor());
        container.addBeanPostProcessor(new PropertyInjectBeanPostProcessor());
        
        // 3. 注册Bean定义
        container.registerBeanDefinition("userService", UserService.class);
        
        // 4. 显示容器初始状态
        container.showContainer();
        
        // 5. 第一次获取Bean（触发创建）
        System.out.println(">>> 第一次获取userService <<<");
        UserService userService1 = (UserService) container.getBean("userService");
        userService1.createUser("张三");
        
        // 6. 显示容器状态
        container.showContainer();
        
        // 7. 第二次获取Bean（从缓存获取）
        System.out.println(">>> 第二次获取userService <<<");
        UserService userService2 = (UserService) container.getBean("userService");
        
        // 8. 验证单例特性
        System.out.println("两次获取的是同一个实例: " + (userService1 == userService2));
        System.out.println("Bean版本: " + userService1.getVersion());
    }
}
```

### 🔍 调试步骤

1. **设置断点**：
   - `AdvancedContainer.createBean()`方法开始
   - 每个后置处理器的方法
2. **观察执行顺序**：
   - 实例化 → 前置处理器 → 初始化 → 后置处理器 → 放入容器
3. **查看变量变化**：
   - Bean实例的创建过程
   - 属性注入的效果

### ✅ 验证结果

应该看到完整的Bean创建生命周期输出，包括各个处理器的执行。

---

## 🧪 Lab 3: 依赖注入基础 - 手动注入依赖

### 目标
理解"依赖注入就是把HashMap里的类实例用name拿出来用"

### 实现步骤

#### 3.1 创建有依赖关系的Bean
```java
// src/main/java/lab3/UserRepository.java
package lab3;

public class UserRepository {
    public void save(String username) {
        System.out.println("UserRepository: 保存用户 " + username + " 到数据库");
    }
    
    public String findUser(String username) {
        System.out.println("UserRepository: 从数据库查找用户 " + username);
        return "User{name='" + username + "', id=123}";
    }
}
```

```java
// src/main/java/lab3/UserService.java
package lab3;

public class UserService {
    private UserRepository userRepository; // 依赖
    
    // 手动设置依赖
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        System.out.println("UserService: 注入依赖 UserRepository");
    }
    
    public void createUser(String username) {
        System.out.println("UserService: 开始创建用户 " + username);
        if (userRepository == null) {
            throw new RuntimeException("UserRepository dependency not injected!");
        }
        userRepository.save(username);
        System.out.println("UserService: 用户创建完成");
    }
    
    public String getUser(String username) {
        System.out.println("UserService: 开始获取用户 " + username);
        if (userRepository == null) {
            throw new RuntimeException("UserRepository dependency not injected!");
        }
        return userRepository.findUser(username);
    }
}
```

#### 3.2 支持依赖注入的容器
```java
// src/main/java/lab3/DIContainer.java
package lab3;

import java.lang.reflect.Method;
import java.util.*;

public class DIContainer {
    private Map<String, Object> beanMap = new HashMap<>();
    private Map<String, Class<?>> beanDefinitions = new HashMap<>();
    private Map<String, List<String>> dependencies = new HashMap<>();
    
    // 注册Bean定义
    public void registerBeanDefinition(String name, Class<?> beanClass) {
        beanDefinitions.put(name, beanClass);
        dependencies.put(name, new ArrayList<>());
        System.out.println("注册Bean定义: " + name + " -> " + beanClass.getSimpleName());
    }
    
    // 添加依赖关系
    public void addDependency(String beanName, String dependencyName) {
        dependencies.get(beanName).add(dependencyName);
        System.out.println("添加依赖关系: " + beanName + " 依赖 " + dependencyName);
    }
    
    // 获取Bean
    public Object getBean(String name) {
        if (beanMap.containsKey(name)) {
            return beanMap.get(name);
        }
        return createBean(name);
    }
    
    // 创建Bean并注入依赖
    private Object createBean(String name) {
        Class<?> beanClass = beanDefinitions.get(name);
        if (beanClass == null) {
            throw new RuntimeException("Bean definition not found: " + name);
        }
        
        System.out.println("\n=== 创建Bean: " + name + " ===");
        
        try {
            // 1. 实例化Bean
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            System.out.println("1. 实例化完成: " + bean.getClass().getSimpleName());
            
            // 2. 注入依赖
            List<String> deps = dependencies.get(name);
            if (!deps.isEmpty()) {
                System.out.println("2. 开始注入依赖...");
                for (String depName : deps) {
                    injectDependency(bean, depName);
                }
            } else {
                System.out.println("2. 无需注入依赖");
            }
            
            // 3. 放入容器
            beanMap.put(name, bean);
            System.out.println("3. Bean放入容器");
            System.out.println("=== Bean创建完成: " + name + " ===\n");
            
            return bean;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + name, e);
        }
    }
    
    // 注入依赖
    private void injectDependency(Object bean, String dependencyName) {
        try {
            // 获取依赖Bean（递归创建）
            Object dependency = getBean(dependencyName);
            
            // 查找setter方法
            String setterName = "set" + capitalize(dependencyName);
            Method[] methods = bean.getClass().getMethods();
            
            for (Method method : methods) {
                if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                    method.invoke(bean, dependency);
                    System.out.println("   注入依赖: " + dependencyName + " -> " + bean.getClass().getSimpleName());
                    return;
                }
            }
            
            throw new RuntimeException("Setter method not found: " + setterName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependency: " + dependencyName, e);
        }
    }
    
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    // 显示容器状态
    public void showContainer() {
        System.out.println("=== 容器状态 ===");
        System.out.println("Bean定义:");
        beanDefinitions.forEach((name, clazz) -> {
            List<String> deps = dependencies.get(name);
            System.out.println("  " + name + " -> " + clazz.getSimpleName() + 
                             (deps.isEmpty() ? "" : " (依赖: " + deps + ")"));
        });
        
        System.out.println("\nBean实例:");
        beanMap.forEach((name, bean) -> 
            System.out.println("  " + name + " -> " + bean.getClass().getSimpleName() + "@" + bean.hashCode())
        );
        System.out.println("===============\n");
    }
}
```

#### 3.3 测试类
```java
// src/main/java/lab3/Lab3Test.java
package lab3;

public class Lab3Test {
    public static void main(String[] args) {
        System.out.println("=== Lab 3: 依赖注入基础 ===\n");
        
        // 1. 创建支持依赖注入的容器
        DIContainer container = new DIContainer();
        
        // 2. 注册Bean定义
        container.registerBeanDefinition("userRepository", UserRepository.class);
        container.registerBeanDefinition("userService", UserService.class);
        
        // 3. 配置依赖关系
        container.addDependency("userService", "userRepository");
        
        // 4. 显示容器初始状态
        container.showContainer();
        
        // 5. 获取UserService（会自动创建并注入依赖）
        System.out.println(">>> 获取userService <<<");
        UserService userService = (UserService) container.getBean("userService");
        
        // 6. 显示容器状态
        container.showContainer();
        
        // 7. 使用UserService（验证依赖注入是否成功）
        System.out.println(">>> 使用userService <<<");
        userService.createUser("张三");
        String user = userService.getUser("张三");
        System.out.println("获取到用户: " + user);
        
        // 8. 验证依赖注入的本质
        System.out.println("\n>>> 验证依赖注入本质 <<<");
        UserRepository repository = (UserRepository) container.getBean("userRepository");
        System.out.println("从容器直接获取的UserRepository: " + repository.hashCode());
        System.out.println("这就是依赖注入的本质：从HashMap中根据name取出实例使用");
    }
}
```

### 🔍 调试步骤

1. **设置断点**：
   - `DIContainer.createBean()`
   - `DIContainer.injectDependency()`
2. **观察依赖创建顺序**：
   - UserService需要UserRepository
   - 先创建UserRepository，再注入到UserService
3. **验证HashMap操作**：
   - 观察Bean从HashMap中的存取过程

---

## 🧪 Lab 4: 注解驱动的依赖注入

### 目标
使用注解简化依赖注入配置

### 实现步骤

#### 4.1 定义注解
```java
// src/main/java/lab4/Component.java
package lab4;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    String value() default "";
}
```

```java
// src/main/java/lab4/Autowired.java
package lab4;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}
```

#### 4.2 注解驱动的Bean
```java
// src/main/java/lab4/UserRepository.java
package lab4;

@Component("userRepository")
public class UserRepository {
    public void save(String username) {
        System.out.println("UserRepository: 保存用户 " + username);
    }
    
    public String findUser(String username) {
        System.out.println("UserRepository: 查找用户 " + username);
        return "User{name='" + username + "'}";
    }
}
```

```java
// src/main/java/lab4/UserService.java
package lab4;

@Component("userService")
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public void createUser(String username) {
        System.out.println("UserService: 创建用户 " + username);
        userRepository.save(username);
    }
    
    public String getUser(String username) {
        return userRepository.findUser(username);
    }
}
```

#### 4.3 注解驱动容器
```java
// src/main/java/lab4/AnnotationContainer.java
package lab4;

import java.lang.reflect.Field;
import java.util.*;

public class AnnotationContainer {
    private Map<String, Object> beanMap = new HashMap<>();
    private Map<String, Class<?>> beanDefinitions = new HashMap<>();
    
    // 扫描包并注册Bean
    public void scan(String packageName) {
        System.out.println("扫描包: " + packageName);
        
        // 这里简化实现，实际应该扫描classpath
        // 手动注册我们的测试类
        registerIfComponent(UserRepository.class);
        registerIfComponent(UserService.class);
    }
    
    private void registerIfComponent(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null) {
            String beanName = component.value();
            if (beanName.isEmpty()) {
                beanName = clazz.getSimpleName().toLowerCase();
            }
            beanDefinitions.put(beanName, clazz);
            System.out.println("发现组件: " + beanName + " -> " + clazz.getSimpleName());
        }
    }
    
    // 获取Bean
    public Object getBean(String name) {
        if (beanMap.containsKey(name)) {
            return beanMap.get(name);
        }
        return createBean(name);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            if (type.isInstance(entry.getValue())) {
                return (T) entry.getValue();
            }
        }
        
        // 如果没找到，尝试创建
        for (Map.Entry<String, Class<?>> entry : beanDefinitions.entrySet()) {
            if (type.isAssignableFrom(entry.getValue())) {
                return (T) createBean(entry.getKey());
            }
        }
        
        throw new RuntimeException("Bean not found for type: " + type.getName());
    }
    
    private Object createBean(String name) {
        Class<?> beanClass = beanDefinitions.get(name);
        if (beanClass == null) {
            throw new RuntimeException("Bean definition not found: " + name);
        }
        
        System.out.println("\n=== 创建Bean: " + name + " ===");
        
        try {
            // 1. 实例化
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            System.out.println("1. 实例化: " + bean.getClass().getSimpleName());
            
            // 2. 先放入容器（解决循环依赖）
            beanMap.put(name, bean);
            
            // 3. 注入依赖
            injectDependencies(bean);
            
            System.out.println("=== Bean创建完成: " + name + " ===\n");
            return bean;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + name, e);
        }
    }
    
    private void injectDependencies(Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                System.out.println("2. 注入字段: " + field.getName());
                
                try {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    Object dependency = getBean(fieldType);
                    field.set(bean, dependency);
                    
                    System.out.println("   注入成功: " + field.getName() + 
                                     " -> " + dependency.getClass().getSimpleName());
                    
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject field: " + field.getName(), e);
                }
            }
        }
    }
    
    public void showContainer() {
        System.out.println("=== 容器状态 ===");
        System.out.println("Bean定义: " + beanDefinitions.size() + "个");
        beanDefinitions.forEach((name, clazz) -> 
            System.out.println("  " + name + " -> " + clazz.getSimpleName())
        );
        
        System.out.println("Bean实例: " + beanMap.size() + "个");
        beanMap.forEach((name, bean) -> 
            System.out.println("  " + name + " -> " + bean.getClass().getSimpleName())
        );
        System.out.println("===============\n");
    }
}
```

#### 4.4 测试类
```java
// src/main/java/lab4/Lab4Test.java
package lab4;

public class Lab4Test {
    public static void main(String[] args) {
        System.out.println("=== Lab 4: 注解驱动的依赖注入 ===\n");
        
        // 1. 创建注解驱动容器
        AnnotationContainer container = new AnnotationContainer();
        
        // 2. 扫描包
        container.scan("lab4");
        
        // 3. 显示容器状态
        container.showContainer();
        
        // 4. 获取Bean
        UserService userService = container.getBean(UserService.class);
        
        // 5. 显示容器状态
        container.showContainer();
        
        // 6. 使用Bean
        userService.createUser("李四");
        String user = userService.getUser("李四");
        System.out.println("获取用户: " + user);
        
        System.out.println("\n注解驱动大大简化了配置！");
    }
}
```

---

## 🧪 Lab 5: AOP实现 - 方法拦截

### 目标
理解"AOP就是允许在方法开始、进行、结束的时间点上搞一个回调函数"

### 实现步骤

#### 5.1 定义AOP相关接口
```java
// src/main/java/lab5/MethodInterceptor.java
package lab5;

public interface MethodInterceptor {
    Object invoke(MethodInvocation invocation) throws Throwable;
}
```

```java
// src/main/java/lab5/MethodInvocation.java
package lab5;

import java.lang.reflect.Method;

public class MethodInvocation {
    private Object target;
    private Method method;
    private Object[] args;
    
    public MethodInvocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }
    
    public Object proceed() throws Throwable {
        return method.invoke(target, args);
    }
    
    // Getters
    public Object getTarget() { return target; }
    public Method getMethod() { return method; }
    public Object[] getArgs() { return args; }
}
```

#### 5.2 创建动态代理工厂
```java
// src/main/java/lab5/ProxyFactory.java
package lab5;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class ProxyFactory {
    private Object target;
    private List<MethodInterceptor> interceptors = new ArrayList<>();
    
    public ProxyFactory(Object target) {
        this.target = target;
    }
    
    public void addInterceptor(MethodInterceptor interceptor) {
        interceptors.add(interceptor);
    }
    
    public Object getProxy() {
        return Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // 创建方法调用对象
                    MethodInvocation invocation = new MethodInvocation(target, method, args);
                    
                    // 如果有拦截器，则通过拦截器链调用
                    if (!interceptors.isEmpty()) {
                        return invokeWithInterceptors(invocation, 0);
                    } else {
                        return invocation.proceed();
                    }
                }
                
                private Object invokeWithInterceptors(MethodInvocation invocation, int index) throws Throwable {
                    if (index >= interceptors.size()) {
                        return invocation.proceed();
                    }
                    
                    MethodInterceptor interceptor = interceptors.get(index);
                    return interceptor.invoke(new MethodInvocation(invocation.getTarget(), 
                                                                  invocation.getMethod(), 
                                                                  invocation.getArgs()) {
                        @Override
                        public Object proceed() throws Throwable {
                            return invokeWithInterceptors(invocation, index + 1);
                        }
                    });
                }
            }
        );
    }
}
```

#### 5.3 创建具体的拦截器
```java
// src/main/java/lab5/LoggingInterceptor.java
package lab5;

public class LoggingInterceptor implements MethodInterceptor {
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        String className = invocation.getTarget().getClass().getSimpleName();
        
        System.out.println("[AOP-前置] 调用方法: " + className + "." + methodName + "()");
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = invocation.proceed();
            
            long endTime = System.currentTimeMillis();
            System.out.println("[AOP-后置] 方法执行完成: " + className + "." + methodName + 
                             "(), 耗时: " + (endTime - startTime) + "ms");
            
            return result;
            
        } catch (Throwable e) {
            System.out.println("[AOP-异常] 方法执行异常: " + className + "." + methodName + 
                             "(), 异常: " + e.getMessage());
            throw e;
        }
    }
}
```

```java
// src/main/java/lab5/SecurityInterceptor.java
package lab5;

public class SecurityInterceptor implements MethodInterceptor {
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        
        // 模拟权限检查
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            System.out.println("[AOP-安全] 检查删除权限...");
            // 这里可以添加实际的权限检查逻辑
            System.out.println("[AOP-安全] 权限检查通过");
        }
        
        return invocation.proceed();
    }
}
```

#### 5.4 创建业务接口和实现
```java
// src/main/java/lab5/UserService.java
package lab5;

public interface UserService {
    void createUser(String username);
    String getUser(String username);
    void deleteUser(String username);
}
```

```java
// src/main/java/lab5/UserServiceImpl.java
package lab5;

public class UserServiceImpl implements UserService {
    
    @Override
    public void createUser(String username) {
        System.out.println("  [业务逻辑] 创建用户: " + username);
        // 模拟一些处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public String getUser(String username) {
        System.out.println("  [业务逻辑] 获取用户: " + username);
        return "User{name='" + username + "'}";
    }
    
    @Override
    public void deleteUser(String username) {
        System.out.println("  [业务逻辑] 删除用户: " + username);
        if ("admin".equals(username)) {
            throw new RuntimeException("不能删除管理员用户");
        }
    }
}
```

#### 5.5 支持AOP的容器
```java
// src/main/java/lab5/AOPContainer.java
package lab5;

import java.util.*;

public class AOPContainer {
    private Map<String, Object> beanMap = new HashMap<>();
    private Map<String, Class<?>> beanDefinitions = new HashMap<>();
    private List<MethodInterceptor> globalInterceptors = new ArrayList<>();
    
    public void registerBean(String name, Class<?> beanClass) {
        beanDefinitions.put(name, beanClass);
    }
    
    public void addGlobalInterceptor(MethodInterceptor interceptor) {
        globalInterceptors.add(interceptor);
        System.out.println("添加全局拦截器: " + interceptor.getClass().getSimpleName());
    }
    
    public Object getBean(String name) {
        if (beanMap.containsKey(name)) {
            return beanMap.get(name);
        }
        return createBean(name);
    }
    
    private Object createBean(String name) {
        Class<?> beanClass = beanDefinitions.get(name);
        if (beanClass == null) {
            throw new RuntimeException("Bean not found: " + name);
        }
        
        try {
            Object target = beanClass.getDeclaredConstructor().newInstance();
            
            // 如果Bean实现了接口，创建AOP代理
            if (beanClass.getInterfaces().length > 0 && !globalInterceptors.isEmpty()) {
                System.out.println("为Bean创建AOP代理: " + name);
                
                ProxyFactory proxyFactory = new ProxyFactory(target);
                for (MethodInterceptor interceptor : globalInterceptors) {
                    proxyFactory.addInterceptor(interceptor);
                }
                
                Object proxy = proxyFactory.getProxy();
                beanMap.put(name, proxy);
                return proxy;
            } else {
                beanMap.put(name, target);
                return target;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + name, e);
        }
    }
}
```

#### 5.6 测试类
```java
// src/main/java/lab5/Lab5Test.java
package lab5;

public class Lab5Test {
    public static void main(String[] args) {
        System.out.println("=== Lab 5: AOP实现 - 方法拦截 ===\n");
        
        // 1. 创建支持AOP的容器
        AOPContainer container = new AOPContainer();
        
        // 2. 添加全局拦截器
        container.addGlobalInterceptor(new LoggingInterceptor());
        container.addGlobalInterceptor(new SecurityInterceptor());
        
        // 3. 注册Bean
        container.registerBean("userService", UserServiceImpl.class);
        
        // 4. 获取Bean（会自动创建AOP代理）
        UserService userService = (UserService) container.getBean("userService");
        
        System.out.println("\n>>> 测试AOP效果 <<<");
        
        // 5. 测试普通方法调用
        System.out.println("\n--- 测试创建用户 ---");
        userService.createUser("张三");
        
        // 6. 测试查询方法
        System.out.println("\n--- 测试查询用户 ---");
        String user = userService.getUser("张三");
        System.out.println("返回结果: " + user);
        
        // 7. 测试删除方法（会触发安全检查）
        System.out.println("\n--- 测试删除普通用户 ---");
        userService.deleteUser("张三");
        
        // 8. 测试异常情况
        System.out.println("\n--- 测试删除管理员（异常情况） ---");
        try {
            userService.deleteUser("admin");
        } catch (Exception e) {
            System.out.println("捕获异常: " + e.getMessage());
        }
        
        System.out.println("\n>>> AOP总结 <<<");
        System.out.println("AOP确实就是在方法执行的关键时间点添加回调函数：");
        System.out.println("- 方法开始前：权限检查、日志记录");
        System.out.println("- 方法执行中：性能监控");
        System.out.println("- 方法结束后：日志记录、清理资源");
        System.out.println("- 异常发生时：异常处理、日志记录");
    }
}
```

---

## 🧪 Lab 6: 循环依赖解决 - 三级缓存

### 目标
理解"三级缓存解决AB类互相依赖的问题"

### 实现步骤

#### 6.1 创建循环依赖的Bean
```java
// src/main/java/lab6/ServiceA.java
package lab6;

public class ServiceA {
    private ServiceB serviceB;
    
    public ServiceA() {
        System.out.println("ServiceA 构造函数执行");
    }
    
    public void setServiceB(ServiceB serviceB) {
        this.serviceB = serviceB;
        System.out.println("ServiceA 注入 ServiceB");
    }
    
    public void doSomething() {
        System.out.println("ServiceA.doSomething() 调用 ServiceB");
        serviceB.doSomething();
    }
    
    public String getName() {
        return "ServiceA";
    }
}
```

```java
// src/main/java/lab6/ServiceB.java
package lab6;

public class ServiceB {
    private ServiceA serviceA;
    
    public ServiceB() {
        System.out.println("ServiceB 构造函数执行");
    }
    
    public void setServiceA(ServiceA serviceA) {
        this.serviceA = serviceA;
        System.out.println("ServiceB 注入 ServiceA");
    }
    
    public void doSomething() {
        System.out.println("ServiceB.doSomething() 调用 ServiceA");
        System.out.println("调用的ServiceA名称: " + serviceA.getName());
    }
    
    public String getName() {
        return "ServiceB";
    }
}
```

#### 6.2 三级缓存容器
```java
// src/main/java/lab6/ThreeLevelCacheContainer.java
package lab6;

import java.lang.reflect.Method;
import java.util.*;

public class ThreeLevelCacheContainer {
    // 一级缓存：完成初始化的Bean
    private Map<String, Object> singletonObjects = new HashMap<>();
    
    // 二级缓存：早期Bean引用（已实例化但未完成初始化）
    private Map<String, Object> earlySingletonObjects = new HashMap<>();
    
    // 三级缓存：Bean工厂（用于创建早期引用）
    private Map<String, ObjectFactory> singletonFactories = new HashMap<>();
    
    // Bean定义
    private Map<String, Class<?>> beanDefinitions = new HashMap<>();
    
    // 依赖关系
    private Map<String, List<String>> dependencies = new HashMap<>();
    
    // 正在创建的Bean集合（用于检测循环依赖）
    private Set<String> singletonsCurrentlyInCreation = new HashSet<>();
    
    public void registerBeanDefinition(String name, Class<?> beanClass) {
        beanDefinitions.put(name, beanClass);
        dependencies.put(name, new ArrayList<>());
    }
    
    public void addDependency(String beanName, String dependencyName) {
        dependencies.get(beanName).add(dependencyName);
    }
    
    public Object getBean(String name) {
        return doGetBean(name);
    }
    
    private Object doGetBean(String name) {
        // 1. 先从一级缓存获取
        Object singleton = getSingleton(name);
        if (singleton != null) {
            System.out.println("从缓存获取Bean: " + name);
            return singleton;
        }
        
        // 2. 检查是否正在创建（循环依赖检测）
        if (singletonsCurrentlyInCreation.contains(name)) {
            System.out.println("检测到循环依赖: " + name);
        }
        
        // 3. 创建Bean
        return createBean(name);
    }
    
    private Object getSingleton(String name) {
        // 从一级缓存获取
        Object singletonObject = singletonObjects.get(name);
        if (singletonObject == null && singletonsCurrentlyInCreation.contains(name)) {
            // 从二级缓存获取
            singletonObject = earlySingletonObjects.get(name);
            if (singletonObject == null) {
                // 从三级缓存获取
                ObjectFactory factory = singletonFactories.get(name);
                if (factory != null) {
                    System.out.println("从三级缓存获取Bean: " + name);
                    singletonObject = factory.getObject();
                    // 放入二级缓存，移除三级缓存
                    earlySingletonObjects.put(name, singletonObject);
                    singletonFactories.remove(name);
                    System.out.println("Bean移入二级缓存: " + name);
                }
            } else {
                System.out.println("从二级缓存获取Bean: " + name);
            }
        } else if (singletonObject != null) {
            System.out.println("从一级缓存获取Bean: " + name);
        }
        
        return singletonObject;
    }
    
    private Object createBean(String name) {
        Class<?> beanClass = beanDefinitions.get(name);
        if (beanClass == null) {
            throw new RuntimeException("Bean definition not found: " + name);
        }
        
        System.out.println("\n=== 开始创建Bean: " + name + " ===");
        
        // 标记正在创建
        singletonsCurrentlyInCreation.add(name);
        
        try {
            // 1. 实例化Bean
            Object bean = doCreateBean(name, beanClass);
            
            // 2. 从二级缓存移除，放入一级缓存
            earlySingletonObjects.remove(name);
            singletonFactories.remove(name);
            singletonObjects.put(name, bean);
            
            System.out.println("Bean创建完成，放入一级缓存: " + name);
            System.out.println("=== Bean创建完成: " + name + " ===\n");
            
            return bean;
            
        } finally {
            // 移除创建标记
            singletonsCurrentlyInCreation.remove(name);
        }
    }
    
    private Object doCreateBean(String name, Class<?> beanClass) {
        try {
            // 1. 实例化
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            
            // 2. 提前暴露Bean（放入三级缓存）
            addSingletonFactory(name, () -> bean);
            
            // 3. 属性注入
            populateBean(bean, name);
            
            return bean;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + name, e);
        }
    }
    
    private void addSingletonFactory(String name, ObjectFactory factory) {
        singletonFactories.put(name, factory);
        System.out.println("Bean放入三级缓存: " + name);
    }
    
    private void populateBean(Object bean, String name) {
        List<String> deps = dependencies.get(name);
        if (deps.isEmpty()) {
            return;
        }
        
        System.out.println("开始属性注入: " + name);
        for (String depName : deps) {
            Object dependency = getBean(depName); // 递归获取依赖
            injectDependency(bean, depName, dependency);
        }
    }
    
    private void injectDependency(Object bean, String dependencyName, Object dependency) {
        try {
            String setterName = "set" + capitalize(dependencyName);
            Method[] methods = bean.getClass().getMethods();
            
            for (Method method : methods) {
                if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                    method.invoke(bean, dependency);
                    System.out.println("属性注入成功: " + bean.getClass().getSimpleName() + 
                                     "." + dependencyName);
                    return;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependency", e);
        }
    }
    
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    // 显示缓存状态
    public void showCacheStatus() {
        System.out.println("=== 三级缓存状态 ===");
        System.out.println("一级缓存（完成初始化）: " + singletonObjects.keySet());
        System.out.println("二级缓存（早期引用）: " + earlySingletonObjects.keySet());
        System.out.println("三级缓存（Bean工厂）: " + singletonFactories.keySet());
        System.out.println("正在创建: " + singletonsCurrentlyInCreation);
        System.out.println("==================\n");
    }
    
    // 函数式接口
    @FunctionalInterface
    public interface ObjectFactory {
        Object getObject();
    }
}
```

#### 6.3 测试类
```java
// src/main/java/lab6/Lab6Test.java
package lab6;

public class Lab6Test {
    public static void main(String[] args) {
        System.out.println("=== Lab 6: 循环依赖解决 - 三级缓存 ===\n");
        
        // 1. 创建三级缓存容器
        ThreeLevelCacheContainer container = new ThreeLevelCacheContainer();
        
        // 2. 注册Bean定义
        container.registerBeanDefinition("serviceA", ServiceA.class);
        container.registerBeanDefinition("serviceB", ServiceB.class);
        
        // 3. 配置循环依赖
        container.addDependency("serviceA", "serviceB");
        container.addDependency("serviceB", "serviceA");
        
        System.out.println("配置完成：ServiceA 依赖 ServiceB，ServiceB 依赖 ServiceA");
        System.out.println("这就是经典的循环依赖问题！\n");
        
        // 4. 显示初始缓存状态
        container.showCacheStatus();
        
        // 5. 获取ServiceA（触发循环依赖解决）
        System.out.println(">>> 开始获取ServiceA <<<");
        ServiceA serviceA = (ServiceA) container.getBean("serviceA");
        
        // 6. 显示最终缓存状态
        container.showCacheStatus();
        
        // 7. 测试Bean功能
        System.out.println(">>> 测试Bean功能 <<<");
        serviceA.doSomething();
        
        // 8. 再次获取Bean（测试缓存）
        System.out.println("\n>>> 再次获取ServiceA（测试缓存）<<<");
        ServiceA serviceA2 = (ServiceA) container.getBean("serviceA");
        System.out.println("两次获取的Bean是同一个对象: " + (serviceA == serviceA2));
        
        // 9. 获取ServiceB
        System.out.println("\n>>> 获取ServiceB <<<");
        ServiceB serviceB = (ServiceB) container.getBean("serviceB");
        serviceB.doSomething();
        
        System.out.println("\n=== Lab 6 完成 ===");
    }
}
```

### 6.4 调试步骤

1. **设置断点**：
   - `ThreeLevelCacheContainer.doGetBean()` 方法入口
   - `getSingleton()` 方法的三个缓存检查点
   - `addSingletonFactory()` 方法
   - `populateBean()` 方法

2. **单步调试流程**：
   ```
   1. getBean("serviceA")
   2. doGetBean("serviceA") - 一级缓存miss
   3. createBean("serviceA")
   4. doCreateBean("serviceA")
   5. 实例化ServiceA
   6. addSingletonFactory("serviceA") - 放入三级缓存
   7. populateBean("serviceA")
   8. getBean("serviceB") - 递归调用
   9. doGetBean("serviceB") - 一级缓存miss
   10. createBean("serviceB")
   11. doCreateBean("serviceB")
   12. 实例化ServiceB
   13. addSingletonFactory("serviceB") - 放入三级缓存
   14. populateBean("serviceB")
   15. getBean("serviceA") - 再次递归调用
   16. doGetBean("serviceA") - 检测到正在创建
   17. getSingleton("serviceA") - 从三级缓存获取
   18. 移入二级缓存
   19. 返回早期ServiceA引用
   20. ServiceB属性注入完成
   21. ServiceB放入一级缓存
   22. ServiceA属性注入完成
   23. ServiceA放入一级缓存
   ```

### 6.5 验证结果

运行程序，观察输出：
- 三级缓存的状态变化
- Bean的创建顺序
- 循环依赖的解决过程
- 最终所有Bean都在一级缓存中

### 6.6 与Spring对比

Spring的三级缓存实现：
```java
// Spring源码中的关键方法
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    Object singletonObject = this.singletonObjects.get(beanName);
    if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
        synchronized (this.singletonObjects) {
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (singletonObject == null && allowEarlyReference) {
                ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                if (singletonFactory != null) {
                    singletonObject = singletonFactory.getObject();
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.singletonFactories.remove(beanName);
                }
            }
        }
    }
    return singletonObject;
}
```

---

## Lab 7: Spring Boot集成对比

### 7.1 目标
- 创建真实的Spring Boot应用
- 对比自实现容器与Spring容器的差异
- 理解Spring Boot的自动配置机制
- 掌握Spring Bean的实际应用

### 7.2 项目结构
```
lab7/
├── pom.xml
├── src/main/java/lab7/
│   ├── SpringBootApp.java
│   ├── config/
│   │   └── BeanConfig.java
│   ├── service/
│   │   ├── UserService.java
│   │   ├── OrderService.java
│   │   └── NotificationService.java
│   ├── controller/
│   │   └── TestController.java
│   └── comparison/
│       └── ContainerComparison.java
└── src/main/resources/
    └── application.yml
```

### 7.3 Maven配置

```xml
<!-- pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.lab</groupId>
    <artifactId>spring-bean-lab7</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 7.4 Spring Boot应用

```java
// src/main/java/lab7/SpringBootApp.java
package lab7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringBootApp {
    public static void main(String[] args) {
        System.out.println("=== Lab 7: Spring Boot集成对比 ===\n");
        
        ConfigurableApplicationContext context = SpringApplication.run(SpringBootApp.class, args);
        
        // 显示容器中的Bean信息
        System.out.println("Spring容器中的Bean数量: " + context.getBeanDefinitionCount());
        
        // 获取我们定义的Bean
        System.out.println("\n=== 我们定义的Bean ===");
        String[] beanNames = context.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (beanName.contains("Service") || beanName.contains("Controller")) {
                System.out.println("Bean: " + beanName + " -> " + 
                                 context.getBean(beanName).getClass().getSimpleName());
            }
        }
        
        // 测试依赖注入
        System.out.println("\n=== 测试依赖注入 ===");
        lab7.service.UserService userService = context.getBean(lab7.service.UserService.class);
        userService.createUser("张三");
        
        System.out.println("\n=== Spring Boot应用启动完成 ===");
        System.out.println("访问 http://localhost:8080/test 查看Web接口");
    }
}
```

### 7.5 业务服务类

```java
// src/main/java/lab7/service/UserService.java
package lab7.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private NotificationService notificationService;
    
    public void createUser(String username) {
        System.out.println("UserService: 创建用户 " + username);
        
        // 调用其他服务
        orderService.createOrder(username);
        notificationService.sendWelcomeMessage(username);
        
        System.out.println("UserService: 用户创建完成");
    }
}
```

```java
// src/main/java/lab7/service/OrderService.java
package lab7.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    @Autowired
    private NotificationService notificationService;
    
    public void createOrder(String username) {
        System.out.println("OrderService: 为用户 " + username + " 创建订单");
        notificationService.sendOrderConfirmation(username);
    }
}
```

```java
// src/main/java/lab7/service/NotificationService.java
package lab7.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    public void sendWelcomeMessage(String username) {
        System.out.println("NotificationService: 发送欢迎消息给 " + username);
    }
    
    public void sendOrderConfirmation(String username) {
        System.out.println("NotificationService: 发送订单确认消息给 " + username);
    }
}
```

### 7.6 Web控制器

```java
// src/main/java/lab7/controller/TestController.java
package lab7.controller;

import lab7.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/test")
    public String test(@RequestParam(defaultValue = "测试用户") String username) {
        userService.createUser(username);
        return "用户创建成功: " + username;
    }
}
```

### 7.7 容器对比分析

```java
// src/main/java/lab7/comparison/ContainerComparison.java
package lab7.comparison;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ContainerComparison implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        analyzeSpringContainer();
    }
    
    private void analyzeSpringContainer() {
        System.out.println("\n=== Spring容器分析 ===");
        
        // 1. Bean数量对比
        System.out.println("Spring容器Bean总数: " + applicationContext.getBeanDefinitionCount());
        System.out.println("我们自实现容器Bean数: 通常只有几个");
        
        // 2. Bean作用域
        System.out.println("\nBean作用域支持:");
        System.out.println("Spring: singleton, prototype, request, session, application");
        System.out.println("自实现: 仅支持singleton");
        
        // 3. 依赖注入方式
        System.out.println("\n依赖注入方式:");
        System.out.println("Spring: 构造器注入、Setter注入、字段注入");
        System.out.println("自实现: 仅支持Setter注入");
        
        // 4. 生命周期回调
        System.out.println("\n生命周期回调:");
        System.out.println("Spring: @PostConstruct, @PreDestroy, InitializingBean, DisposableBean");
        System.out.println("自实现: 基础的前置/后置处理器");
        
        // 5. AOP支持
        System.out.println("\nAOP支持:");
        System.out.println("Spring: 完整的AspectJ支持，多种通知类型");
        System.out.println("自实现: 简单的方法拦截");
        
        // 6. 配置方式
        System.out.println("\n配置方式:");
        System.out.println("Spring: 注解配置、Java配置、XML配置");
        System.out.println("自实现: 编程式配置");
        
        System.out.println("==================\n");
    }
}
```

### 7.8 配置文件

```yaml
# src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: spring-bean-lab7

logging:
  level:
    org.springframework: INFO
    lab7: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: beans,health,info
```

### 7.9 调试步骤

1. **启动应用**：
   ```bash
   mvn spring-boot:run
   ```

2. **观察启动日志**：
   - Bean的创建顺序
   - 自动配置的Bean
   - 依赖注入过程

3. **访问Actuator端点**：
   ```
   http://localhost:8080/actuator/beans
   ```

4. **测试Web接口**：
   ```
   http://localhost:8080/test?username=张三
   ```

5. **断点调试**：
   - 在`UserService.createUser()`设置断点
   - 观察依赖注入的Bean
   - 查看Spring容器的内部状态

### 7.10 验证结果

1. **功能验证**：
   - 所有服务正常工作
   - 依赖注入成功
   - Web接口响应正常

2. **性能对比**：
   - Spring容器启动时间
   - Bean创建效率
   - 内存使用情况

3. **特性对比**：
   - 自动配置vs手动配置
   - 注解驱动vs编程式配置
   - 完整生态vs简单实现

---

## 总结与进阶

### 学习成果检验

完成所有Lab后，你应该能够：

1. **理解Spring容器本质**：
   - 容器就是一个高级的HashMap
   - Bean的注册、创建、管理机制
   - 依赖注入的实现原理

2. **掌握Bean生命周期**：
   - 实例化 → 属性注入 → 初始化 → 使用 → 销毁
   - 前置/后置处理器的作用时机
   - 生命周期回调的使用

3. **理解AOP机制**：
   - 动态代理的实现原理
   - 切面编程的应用场景
   - 方法拦截和增强

4. **解决循环依赖**：
   - 三级缓存的设计思想
   - 早期引用的暴露机制
   - 循环依赖的检测和解决

5. **Spring Boot集成**：
   - 自动配置的便利性
   - 注解驱动开发
   - 生产级特性

### 进阶学习路径

1. **深入Spring源码**：
   - `AbstractApplicationContext`
   - `DefaultListableBeanFactory`
   - `AbstractAutowireCapableBeanFactory`

2. **扩展学习**：
   - Spring MVC原理
   - Spring Data JPA
   - Spring Security
   - Spring Cloud微服务

3. **实践项目**：
   - 构建完整的Web应用
   - 微服务架构实践
   - 性能优化和监控

### 关键概念回顾

```
Spring Bean知识图谱:

容器(Container)
├── Bean定义注册
├── Bean实例化
├── 依赖注入
├── 生命周期管理
└── 作用域管理

生命周期(Lifecycle)
├── 实例化(Instantiation)
├── 属性注入(Population)
├── 初始化(Initialization)
├── 使用(In Use)
└── 销毁(Destruction)

依赖注入(DI)
├── 构造器注入
├── Setter注入
├── 字段注入
└── 循环依赖解决

AOP(面向切面编程)
├── 动态代理
├── 切点表达式
├── 通知类型
└── 切面配置

高级特性
├── 三级缓存
├── 自动配置
├── 条件注册
└── 事件机制
```

通过这个循序渐进的实验，你已经从最基础的HashMap概念，一步步构建出了对Spring Bean的深度理解。每个Lab都是前一个的基础，形成了完整的知识体系。现在你可以自信地说：我真正理解了Spring Bean！