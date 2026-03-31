package com.example.service;

import java.util.List;

/**
 * 订单实体
 */
public class Order {
    private List<OrderItem> items;
    private int vipLevel;
    private double totalAmount;

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public int getVipLevel() { return vipLevel; }
    public void setVipLevel(int vipLevel) { this.vipLevel = vipLevel; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
