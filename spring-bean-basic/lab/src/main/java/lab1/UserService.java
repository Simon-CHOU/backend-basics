package lab1;

/**
 * 用户服务类 - 模拟业务Bean
 */
public class UserService {
    
    private String serviceName;
    
    public UserService() {
        this.serviceName = "UserService";
        System.out.println("UserService 构造函数被调用");
    }
    
    public UserService(String serviceName) {
        this.serviceName = serviceName;
        System.out.println("UserService 带参构造函数被调用: " + serviceName);
    }
    
    public void createUser(String username) {
        System.out.println(serviceName + ": 创建用户 " + username);
    }
    
    public void deleteUser(String username) {
        System.out.println(serviceName + ": 删除用户 " + username);
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    @Override
    public String toString() {
        return "UserService{serviceName='" + serviceName + "'}";
    }
}