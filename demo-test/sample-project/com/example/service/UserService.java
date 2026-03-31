package com.example.service;

/**
 * 用户服务 - 简单示例
 */
public class UserService {

    /**
     * 简单方法 - 复杂度 1
     */
    public String greet(String name) {
        return "Hello, " + name;
    }

    /**
     * 中等复杂度方法 - 复杂度 5
     */
    public String getUserLevel(int score) {
        if (score >= 90) {
            return "优秀";
        } else if (score >= 80) {
            return "良好";
        } else if (score >= 60) {
            return "及格";
        } else {
            return "不及格";
        }
    }

    /**
     * 复杂方法 - 复杂度 10
     */
    public void processOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("订单不能为空");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单必须包含商品");
        }

        // 验证库存
        for (OrderItem item : order.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("商品数量必须大于 0");
            }
            if (item.getPrice() < 0) {
                throw new IllegalArgumentException("商品价格不能为负");
            }
        }

        // 计算总价
        double total = 0;
        for (OrderItem item : order.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }

        // 应用折扣
        if (order.getVipLevel() == 1) {
            total *= 0.95;
        } else if (order.getVipLevel() == 2) {
            total *= 0.90;
        } else if (order.getVipLevel() >= 3) {
            total *= 0.85;
        }

        order.setTotalAmount(total);
    }
}
