package com.simon.benchmark.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDetailView {

    private String orderId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private Instant orderCreatedAt;
    private List<OrderItemView> items;
    private String userName;
    private String userEmail;
    private String userPhone;
    private Instant userRegisteredAt;

    public OrderDetailView() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Instant getOrderCreatedAt() {
        return orderCreatedAt;
    }

    public void setOrderCreatedAt(Instant orderCreatedAt) {
        this.orderCreatedAt = orderCreatedAt;
    }

    public List<OrderItemView> getItems() {
        return items;
    }

    public void setItems(List<OrderItemView> items) {
        this.items = items;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public Instant getUserRegisteredAt() {
        return userRegisteredAt;
    }

    public void setUserRegisteredAt(Instant userRegisteredAt) {
        this.userRegisteredAt = userRegisteredAt;
    }

    public static class OrderItemView {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;

        public OrderItemView() {
        }

        public OrderItemView(String productId, String productName, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
