package com.example.service;

/**
 * 支付服务 - 高复杂度示例
 */
public class PaymentService {

    /**
     * 处理支付 - 非常复杂的方法 - 复杂度 15+
     */
    public PaymentResult processPayment(PaymentRequest request) {
        // 参数验证
        if (request == null) {
            return PaymentResult.error("请求不能为空");
        }
        if (request.getAmount() <= 0) {
            return PaymentResult.error("支付金额必须大于 0");
        }
        if (request.getUserId() == null) {
            return PaymentResult.error("用户 ID 不能为空");
        }

        // 用户验证
        User user = getUser(request.getUserId());
        if (user == null) {
            return PaymentResult.error("用户不存在");
        }
        if (!user.isActive()) {
            return PaymentResult.error("用户账户已禁用");
        }

        // 余额检查
        if (user.getBalance() < request.getAmount()) {
            return PaymentResult.error("余额不足");
        }

        // 风险控制
        if (request.getAmount() > 10000) {
            if (!user.isVerified()) {
                return PaymentResult.error("大额支付需要实名认证");
            }
            if (request.getAmount() > 50000) {
                return PaymentResult.error("单笔支付不能超过 5 万");
            }
        }

        // 处理支付
        try {
            // 扣减余额
            user.setBalance(user.getBalance() - request.getAmount());
            updateUser(user);

            // 创建支付记录
            PaymentRecord record = new PaymentRecord();
            record.setUserId(request.getUserId());
            record.setAmount(request.getAmount());
            record.setStatus("SUCCESS");
            saveRecord(record);

            // 发送通知
            if (request.getAmount() >= 1000) {
                sendNotification(user, "大额支付通知");
            }

            return PaymentResult.success("支付成功");

        } catch (Exception e) {
            return PaymentResult.error("支付失败：" + e.getMessage());
        }
    }

    // 辅助方法
    private User getUser(String userId) { return null; }
    private void updateUser(User user) {}
    private void saveRecord(PaymentRecord record) {}
    private void sendNotification(User user, String msg) {}
}

class User {
    private String id;
    private double balance;
    private boolean active;
    private boolean verified;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}

class PaymentRequest {
    private String userId;
    private double amount;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}

class PaymentResult {
    private boolean success;
    private String message;
    public static PaymentResult success(String msg) {
        PaymentResult r = new PaymentResult();
        r.success = true;
        r.message = msg;
        return r;
    }
    public static PaymentResult error(String msg) {
        PaymentResult r = new PaymentResult();
        r.success = false;
        r.message = msg;
        return r;
    }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

class PaymentRecord {
    private String userId;
    private double amount;
    private String status;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
