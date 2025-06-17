package com.contract.backend.common.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContractIntegrityVerificationDTO {

    private Long contractVersionId;
    private VerificationStep dbVerification; // DB 내부 데이터 검증 결과
    private VerificationStep blockchainVerification; // DB 데이터와 블록체인 데이터 비교 검증 결과
    private boolean overallSuccess;
    private String message;
    private LocalDateTime verifiedAt;

    public enum VerificationStatus {
        NOT_CHECKED,
        SUCCESS,
        FAILED,
        DATA_NOT_FOUND, // DB 또는 블록체인에서 필요한 데이터를 찾을 수 없음
        ERROR // 검증 중 오류 발생
    }

    public static class VerificationStep {
        private VerificationStatus status;
        private String details;
        private List<String> discrepancies; // 불일치 항목 상세

        public VerificationStep(VerificationStatus status, String details) {
            this.status = status;
            this.details = details;
            this.discrepancies = new ArrayList<>();
        }

        // Getters & Setters
        public VerificationStatus getStatus() { return status; }
        public void setStatus(VerificationStatus status) { this.status = status; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public List<String> getDiscrepancies() { return discrepancies; }
        public void setDiscrepancies(List<String> discrepancies) { this.discrepancies = discrepancies; }
        public void addDiscrepancy(String discrepancy) { this.discrepancies.add(discrepancy); }
    }

    public ContractIntegrityVerificationDTO(Long contractVersionId) {
        this.contractVersionId = contractVersionId;
        this.dbVerification = new VerificationStep(VerificationStatus.NOT_CHECKED, "DB data integrity check not performed yet.");
        this.blockchainVerification = new VerificationStep(VerificationStatus.NOT_CHECKED, "Blockchain data comparison not performed yet.");
        this.overallSuccess = false;
        this.verifiedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getContractVersionId() { return contractVersionId; }
    public void setContractVersionId(Long contractVersionId) { this.contractVersionId = contractVersionId; }
    public VerificationStep getDbVerification() { return dbVerification; }
    public void setDbVerification(VerificationStep dbVerification) { this.dbVerification = dbVerification; }
    public VerificationStep getBlockchainVerification() { return blockchainVerification; }
    public void setBlockchainVerification(VerificationStep blockchainVerification) { this.blockchainVerification = blockchainVerification; }
    public boolean isOverallSuccess() { return overallSuccess; }
    public void setOverallSuccess(boolean overallSuccess) { this.overallSuccess = overallSuccess; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}