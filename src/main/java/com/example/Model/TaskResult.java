package com.example.Model;

public class TaskResult {
    private final String url;
    private final String status;
    private final long duration;
    private final int dataSize;

    public TaskResult(String url, String status, long duration, int dataSize) {
        this.url = url;
        this.status = status;
        this.duration = duration;
        this.dataSize = dataSize;
    }

    public String getUrl() {
        return url;
    }

    public String getStatus() {
        return status;
    }

    public long getDuration() {
        return duration;
    }

    public int getDataSize() {
        return dataSize;
    }
}

