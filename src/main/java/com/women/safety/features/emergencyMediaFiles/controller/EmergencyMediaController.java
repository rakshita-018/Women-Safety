package com.women.safety.features.emergencyMediaFiles.controller;


import com.women.safety.features.authentication.security.CustomUserDetails;
import com.women.safety.features.emergencyMediaFiles.dto.EnhancedSOSRequestDTO;
import com.women.safety.features.emergencyMediaFiles.dto.MediaSummaryDTO;
import com.women.safety.features.emergencyMediaFiles.dto.MediaUploadResponseDTO;
import com.women.safety.features.emergencyMediaFiles.model.EmergencyMedia;
import com.women.safety.features.emergencyMediaFiles.repository.EmergencyMediaRepository;
import com.women.safety.features.emergencyMediaFiles.service.EmergencyMediaService;
import com.women.safety.features.emergencySOS.dto.EmergencyAlertResponseDTO;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyMediaController {

    private final EmergencyMediaService mediaService;
    private final EmergencyMediaRepository mediaRepository;

    public EmergencyMediaController(EmergencyMediaService mediaService, EmergencyMediaRepository mediaRepository) {
        this.mediaService = mediaService;
        this.mediaRepository = mediaRepository;
    }

    // ==================== Enhanced SOS Endpoints ====================

    /**
     * STEP 1: Trigger Fast SOS (Instant SMS)
     * POST /api/emergency/sos/fast
     */
    @PostMapping("/sos/fast")
    public ResponseEntity<EmergencyAlertResponseDTO> triggerFastSOS(
            @Valid @RequestBody EnhancedSOSRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EmergencyAlertResponseDTO response = mediaService.triggerFastSOS(
                userDetails.getUsername(), request);

        return ResponseEntity.ok(response);
    }

    // ==================== Media Upload Endpoints (STEP 2) ====================

    /**
     * Upload Audio Recording
     * POST /api/emergency/media/upload/audio
     *
     * Form Data:
     * - alertId: Long
     * - file: MultipartFile (.aac, .m4a, .mp3)
     * - durationSeconds: Integer (optional)
     * - latitude: Double (optional)
     * - longitude: Double (optional)
     */
    @PostMapping("/media/upload/audio")
    public ResponseEntity<MediaUploadResponseDTO> uploadAudio(
            @RequestParam("alertId") Long alertId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MediaUploadResponseDTO response = mediaService.uploadAudio(
                userDetails.getUsername(), alertId, file, durationSeconds, latitude, longitude);

        return ResponseEntity.ok(response);
    }

    /**
     * Upload Photo
     * POST /api/emergency/media/upload/photo
     */
    @PostMapping("/media/upload/photo")
    public ResponseEntity<MediaUploadResponseDTO> uploadPhoto(
            @RequestParam("alertId") Long alertId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MediaUploadResponseDTO response = mediaService.uploadPhoto(
                userDetails.getUsername(), alertId, file, latitude, longitude);

        return ResponseEntity.ok(response);
    }

    /**
     * Upload Video
     * POST /api/emergency/media/upload/video
     */
    @PostMapping("/media/upload/video")
    public ResponseEntity<MediaUploadResponseDTO> uploadVideo(
            @RequestParam("alertId") Long alertId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MediaUploadResponseDTO response = mediaService.uploadVideo(
                userDetails.getUsername(), alertId, file, durationSeconds, latitude, longitude);

        return ResponseEntity.ok(response);
    }

    // ==================== Media Retrieval Endpoints ====================

    /**
     * Get all media for an alert
     * GET /api/emergency/media/alert/{alertId}
     */
    @GetMapping("/media/alert/{alertId}")
    public ResponseEntity<MediaSummaryDTO> getAlertMedia(
            @PathVariable Long alertId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MediaSummaryDTO summary = mediaService.getAlertMedia(userDetails.getUsername(), alertId);

        return ResponseEntity.ok(summary);
    }

    /**
     * View/Download specific media file
     * GET /api/emergency/media/view/{mediaId}
     *
     * Works for both LOCAL and R2 storage modes
     */
    @GetMapping("/media/view/{mediaId}")
    public ResponseEntity<Resource> viewMedia(@PathVariable Long mediaId) {

        EmergencyMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        byte[] fileBytes = mediaService.getMediaFile(mediaId);
        ByteArrayResource resource = new ByteArrayResource(fileBytes);

        // Determine content type
        String contentType = media.getMimeType() != null ?
                media.getMimeType() : "application/octet-stream";

        // Set proper filename for download
        String fileName = media.getFileName() != null ?
                media.getFileName() : "evidence_file";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\"")
                .contentLength(fileBytes.length)
                .body(resource);
    }

    /**
     * Delete media file
     * DELETE /api/emergency/media/{mediaId}
     */
    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Map<String, String>> deleteMedia(
            @PathVariable Long mediaId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        mediaService.deleteMedia(userDetails.getUsername(), mediaId);

        return ResponseEntity.ok(Map.of(
                "message", "Media deleted successfully",
                "mediaId", mediaId.toString()
        ));
    }

    // ==================== Utility Endpoints ====================

    /**
     * Test media upload system
     * GET /api/emergency/media/test
     */
    @GetMapping("/media/test")
    public ResponseEntity<Map<String, String>> testMediaSystem() {
        return ResponseEntity.ok(Map.of(
                "status", "Media upload system is ready",
                "supported_audio", ".aac, .m4a, .mp3",
                "supported_photo", ".jpg, .png",
                "supported_video", ".mp4, .mov",
                "max_file_size", "10 MB"
        ));
    }
}