package com.contract.backend.common.dto;

// 블록체인 트랜잭션 결과 DTO
public class BlockchainTransactionResponseDTO {
    private String transactionId; // Fabric Tx ID
    private String status;        // "SUCCESS", "FAILED", "PENDING" 등
    private String message;       // 추가 메시지 (오류 등)

    public BlockchainTransactionResponseDTO(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    // Getters & Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}