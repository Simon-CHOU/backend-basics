package com.simon.circledependency;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解决循环依赖
 * https://mp.weixin.qq.com/s/Yxpu1S6oztXDoibrGjyPyw
 * <p>让类的创建和属性的填充分离，先创建出半成品Bean，再处理属性的填充，完成成品Bean的提供</p>
 */
public class CircleTest {
    private final static Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        System.out.println(getBean(B.class).getA());
        System.out.println(getBean(A.class).getB());
    }

    /**
     * 解决循环依赖
     *
     * @param beanClass
     * @param <T>
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static <T> T getBean(Class<T> beanClass) throws InstantiationException, IllegalAccessException {
        String beanName = beanClass.getSimpleName().toLowerCase();
        if (singletonObjects.containsKey(beanName)) {
            return (T) singletonObjects.get(beanName);
        }
        // 实例化对象入缓存
        Object obj = beanClass.newInstance();
        singletonObjects.put(beanName, obj);
        // 属性填充补全对象
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldClass = field.getType();
            String fieldBeanName = fieldClass.getSimpleName().toLowerCase();
            field.set(obj, singletonObjects.containsKey(fieldBeanName) ?
                    singletonObjects.get(fieldBeanName) : getBean(fieldClass));
            field.setAccessible(false);
        }
        return (T) obj;
    }


}

class A {
    private B b;

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }
}

class B {
    private A a;

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }
}