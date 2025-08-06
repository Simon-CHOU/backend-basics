package lab1;

import java.util.HashMap;
import java.util.Map;

/**
 * Lab 1: 简单容器实现
 * 这是一个最基础的Bean容器，本质上就是一个HashMap
 */
public class SimpleContainer {
    
    // 核心：用HashMap存储Bean实例
    private Map<String, Object> beans = new HashMap<>();
    
    /**
     * 注册Bean到容器中
     * @param name Bean的名称
     * @param bean Bean的实例
     */
    public void registerBean(String name, Object bean) {
        System.out.println("注册Bean: " + name + " -> " + bean.getClass().getSimpleName());
        beans.put(name, bean);
    }
    
    /**
     * 从容器中获取Bean
     * @param name Bean的名称
     * @return Bean实例
     */
    public Object getBean(String name) {
        Object bean = beans.get(name);
        if (bean == null) {
            throw new RuntimeException("Bean not found: " + name);
        }
        System.out.println("获取Bean: " + name + " -> " + bean.getClass().getSimpleName());
        return bean;
    }
    
    /**
     * 检查容器中是否包含指定的Bean
     * @param name Bean的名称
     * @return 是否包含
     */
    public boolean containsBean(String name) {
        return beans.containsKey(name);
    }
    
    /**
     * 获取容器中所有Bean的名称
     * @return Bean名称数组
     */
    public String[] getBeanNames() {
        return beans.keySet().toArray(new String[0]);
    }
    
    /**
     * 获取容器中Bean的数量
     * @return Bean数量
     */
    public int getBeanCount() {
        return beans.size();
    }
    
    /**
     * 显示容器状态
     */
    public void showContainerStatus() {
        System.out.println("=== 容器状态 ===");
        System.out.println("Bean总数: " + getBeanCount());
        System.out.println("Bean列表:");
        for (String name : getBeanNames()) {
            Object bean = beans.get(name);
            System.out.println("  " + name + " -> " + bean.getClass().getSimpleName() + 
                             " [" + bean.hashCode() + "]");
        }
        System.out.println("===============\n");
    }
}