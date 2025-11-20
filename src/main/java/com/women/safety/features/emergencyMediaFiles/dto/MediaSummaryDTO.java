package com.women.safety.features.emergencyMediaFiles.dto;

import lombok.Data;

import java.util.List;

@Data
public class MediaSummaryDTO {

    private Long alertId;
    private Integer totalMediaCount;
    private Integer audioCount;
    private Integer photoCount;
    private Integer videoCount;
    private Integer completedCount;
    private Integer pendingCount;
    private String message;
    private List<MediaItemDTO> media;

    @Data
    public static class MediaItemDTO {
        private Long id;
        private String type;
        private String fileName;
        private String fileSize;
        private Integer durationSeconds;
        private String status;
        private String viewUrl;
        private String thumbnailUrl;
    }
}
