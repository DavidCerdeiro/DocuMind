package com.davidcerdeiro.documind.dto;

public record JobStatus(String status, int progress, String message) {
    public JobStatus(String status, int progress) {
        this(status, progress, null);
    }
}
