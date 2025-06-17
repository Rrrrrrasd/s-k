package com.contract.backend.common.dto;



// ContractUploadRequestDTO 와 필드가 같다면 재사용하거나,
// 수정 시 일부 필드만 변경 가능하다면 필요한 필드만 포함할 수 있습니다.
// 여기서는 제목, 설명 변경도 가능하다고 가정합니다.
public class ContractUpdateRequestDTO {
    private String title;
    private String description;

    // 참여자 목록은 이 단계에서 변경하지 않는다고 가정합니다.
    // 만약 참여자 변경도 이 요청에서 처리하려면 List<UUID> participantIds; 추가

    public ContractUpdateRequestDTO() {
    }

    public ContractUpdateRequestDTO(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
