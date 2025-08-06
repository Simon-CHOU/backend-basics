package lab1;

/**
 * 订单服务类 - 另一个业务Bean
 */
public class OrderService {
    
    private String version;
    
    public OrderService() {
        this.version = "1.0";
        System.out.println("OrderService 构造函数被调用");
    }
    
    public void createOrder(String orderId) {
        System.out.println("OrderService v" + version + ": 创建订单 " + orderId);
    }
    
    public void cancelOrder(String orderId) {
        System.out.println("OrderService v" + version + ": 取消订单 " + orderId);
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "OrderService{version='" + version + "'}";
    }
}