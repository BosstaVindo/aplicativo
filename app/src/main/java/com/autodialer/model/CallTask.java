package com.autodialer.model;

public class CallTask {
    private String number;
    private int attempts;
    private int maxRetries;
    private long timestamp;
    private String status;

    public CallTask(String number, int attempts, int maxRetries) {
        this.number = number;
        this.attempts = attempts;
        this.maxRetries = maxRetries;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CallTask{" +
                "number='" + number + '\'' +
                ", attempts=" + attempts +
                ", maxRetries=" + maxRetries +
                ", status='" + status + '\'' +
                '}';
    }
}
