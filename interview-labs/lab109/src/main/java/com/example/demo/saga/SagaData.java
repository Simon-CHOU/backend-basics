package com.example.demo.saga;

import java.util.HashMap;
import java.util.Map;

/**
 * Saga数据载体
 * 在Saga步骤之间传递数据
 */
public class SagaData {
    
    private final Map<String, Object> data;
    
    public SagaData() {
        this.data = new HashMap<>();
    }
    
    public SagaData(Map<String, Object> initialData) {
        this.data = new HashMap<>(initialData);
    }
    
    public void put(String key, Object value) {
        data.put(key, value);
    }
    
    public Object get(String key) {
        return data.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new ClassCastException("Cannot cast " + value.getClass() + " to " + type);
    }
    
    public String getString(String key) {
        return get(key, String.class);
    }
    
    public Long getLong(String key) {
        Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            return Long.valueOf((String) value);
        }
        return null;
    }
    
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }
    
    public void remove(String key) {
        data.remove(key);
    }
    
    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }
    
    public void putAll(Map<String, Object> additionalData) {
        data.putAll(additionalData);
    }
    
    @Override
    public String toString() {
        return "SagaData{" + data + '}';
    }
}