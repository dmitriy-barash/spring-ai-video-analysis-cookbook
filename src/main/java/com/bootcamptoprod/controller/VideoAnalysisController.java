package com.bootcamptoprod.controller;

import com.bootcamptoprod.dto.Base64VideoAnalysisRequest;
import com.bootcamptoprod.dto.VideoAnalysisRequest;
import com.bootcamptoprod.dto.VideoAnalysisResponse;
import com.bootcamptoprod.exception.VideoProcessingException;
import com.bootcamptoprod.service.VideoAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Exposes the REST API endpoints for all four video analysis scenarios.
 * This controller acts as the entry point for all incoming web requests
 * and delegates the core logic to the VideoAnalysisService.
 */
@RestController
@RequestMapping("/api/v1/video/analysis")
public class VideoAnalysisController {

    private final VideoAnalysisService videoAnalysisService;

    public VideoAnalysisController(VideoAnalysisService videoAnalysisService) {
        this.videoAnalysisService = videoAnalysisService;
    }

    /**
     * SCENARIO 1: Analyze a single video file from the classpath (e.g., src/main/resources/video).
     */
    @PostMapping("/from-classpath")
    public ResponseEntity<VideoAnalysisResponse> analyzeFromClasspath(@RequestBody VideoAnalysisRequest request) {
        VideoAnalysisResponse response = videoAnalysisService.analyzeVideoFromClasspath(request.fileName(), request.prompt());
        return ResponseEntity.ok(response);
    }

    /**
     * SCENARIO 2: Analyze one or more video files uploaded by the user.
     * This endpoint handles multipart/form-data requests.
     */
    @PostMapping(value = "/from-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoAnalysisResponse> analyzeFromFiles(@RequestParam("videoFiles") List<MultipartFile> videoFiles, @RequestParam("prompt") String prompt) {
        VideoAnalysisResponse response = videoAnalysisService.analyzeVideoFromFile(videoFiles, prompt);
        return ResponseEntity.ok(response);
    }

    /**
     * SCENARIO 3: Analyze one or more video files from a list of URLs.
     */
    @PostMapping("/from-urls")
    public ResponseEntity<VideoAnalysisResponse> analyzeFromUrls(@RequestBody VideoAnalysisRequest request) {
        VideoAnalysisResponse response = videoAnalysisService.analyzeVideoFromUrl(request.videoUrls(), request.prompt());
        return ResponseEntity.ok(response);
    }

    /**
     * SCENARIO 4: Analyze one or more video files from Base64-encoded strings.
     */
    @PostMapping("/from-base64")
    public ResponseEntity<VideoAnalysisResponse> analyzeFromBase64(@RequestBody Base64VideoAnalysisRequest request) {
        VideoAnalysisResponse response = videoAnalysisService.analyzeVideoFromBase64(request.base64VideoList(), request.prompt());
        return ResponseEntity.ok(response);
    }

    /**
     * Centralized exception handler for this controller.
     * Catches our custom exception from the service layer and returns a clean
     * HTTP 400 Bad Request with the error message.
     */
    @ExceptionHandler(VideoProcessingException.class)
    public ResponseEntity<VideoAnalysisResponse> handleVideoProcessingException(VideoProcessingException ex) {
        // You can also create a dedicated error response DTO if you prefer
        return ResponseEntity.badRequest().body(new VideoAnalysisResponse(ex.getMessage()));
    }
}