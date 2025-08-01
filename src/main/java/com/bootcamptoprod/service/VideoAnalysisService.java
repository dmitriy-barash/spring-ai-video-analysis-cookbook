package com.bootcamptoprod.service;

import com.bootcamptoprod.dto.Base64Video;
import com.bootcamptoprod.dto.VideoAnalysisResponse;
import com.bootcamptoprod.exception.VideoProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains the core business logic for the application. This service handles
 * converting all video input types into a common format (Spring AI Media objects)
 * and uses the ChatClient to communicate with the multimodal AI model.
 */
@Service
public class VideoAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(VideoAnalysisService.class);

    // A single, reusable system prompt that defines the AI's persona and rules for video.
    private static final String SYSTEM_PROMPT_TEMPLATE = getSystemPrompt();

    // A constant to programmatically check if the AI followed our rules.
    private static final String AI_ERROR_RESPONSE = "Error: I can only analyze video and answer related questions.";

    private final ChatClient chatClient;

    // The ChatClient.Builder is injected by Spring, allowing us to build the client.
    public VideoAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // --- IMPLEMENTATION FOR SCENARIO 1: CLASSPATH ---
    public VideoAnalysisResponse analyzeVideoFromClasspath(String fileName, String prompt) {
        validatePrompt(prompt);

        if (!StringUtils.hasText(fileName)) {
            throw new VideoProcessingException("File name cannot be empty.");
        }

        Resource videoResource = new ClassPathResource("video/" + fileName);
        if (!videoResource.exists()) {
            throw new VideoProcessingException("File not found in classpath: video/" + fileName);
        }

        Media videoMedia = new Media(MimeType.valueOf("video/mp4"), videoResource);
        return performAnalysis(prompt, List.of(videoMedia));
    }

    // --- IMPLEMENTATION FOR SCENARIO 2: MULTIPART FILES ---
    public VideoAnalysisResponse analyzeVideoFromFile(List<MultipartFile> videos, String prompt) {
        validatePrompt(prompt);

        if (videos == null || videos.isEmpty() || videos.stream().allMatch(MultipartFile::isEmpty)) {
            throw new VideoProcessingException("Video files list cannot be empty.");
        }

        List<Media> mediaList = videos.stream()
                .filter(file -> !file.isEmpty())
                .map(this::convertMultipartFileToMedia)
                .collect(Collectors.toList());

        return performAnalysis(prompt, mediaList);
    }

    // --- IMPLEMENTATION FOR SCENARIO 3: AUDIO URLS ---
    public VideoAnalysisResponse analyzeVideoFromUrl(List<String> videoUrls, String prompt) {
        validatePrompt(prompt);

        if (videoUrls == null || videoUrls.isEmpty()) {
            throw new VideoProcessingException("Video URL list cannot be empty.");
        }

        List<Media> mediaList = videoUrls.stream()
                .map(this::convertUrlToMedia)
                .collect(Collectors.toList());

        return performAnalysis(prompt, mediaList);
    }

    // --- IMPLEMENTATION FOR SCENARIO 4: BASE64 ---
    public VideoAnalysisResponse analyzeVideoFromBase64(List<Base64Video> base64Videos, String prompt) {
        validatePrompt(prompt);

        if (base64Videos == null || base64Videos.isEmpty()) {
            throw new VideoProcessingException("Base64 video list cannot be empty.");
        }

        List<Media> mediaList = base64Videos.stream()
                .map(this::convertBase64ToMedia)
                .collect(Collectors.toList());

        return performAnalysis(prompt, mediaList);
    }

    /**
     * Core method to communicate with the AI model.
     * It sends the text prompt and a list of video media objects for analysis.
     */
    private VideoAnalysisResponse performAnalysis(String prompt, List<Media> mediaList) {

        if (mediaList.isEmpty()) {
            throw new VideoProcessingException("No valid video files were provided for analysis.");
        }

        // This is where the magic happens: combining text and media in one call.
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT_TEMPLATE)
                .user(userSpec -> userSpec
                        .text(prompt)
                        .media(mediaList.toArray(new Media[0])))
                .call()
                .content();

        // Check if the AI responded with our predefined error message (a "guardrail").
        if (AI_ERROR_RESPONSE.equalsIgnoreCase(response)) {
            throw new VideoProcessingException("The provided prompt is not related to video analysis.");
        }

        return new VideoAnalysisResponse(response);
    }

    /**
     * Helper to convert a MultipartFile into a Spring AI Media object.
     */
    private Media convertMultipartFileToMedia(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            MimeType mimeType = determineVideoMimeType(contentType);
            return new Media(mimeType, file.getResource());
        } catch (Exception e) {
            throw new VideoProcessingException("Failed to process uploaded file: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Helper to download a video from a URL and convert it into a Media object.
     */
    private Media convertUrlToMedia(String videoUrl) {
        try {
            log.info("Processing video from URL: {}", videoUrl);
            URL url = new URL(videoUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new VideoProcessingException("Invalid or non-video MIME type for URL: " + videoUrl);
            }

            Resource videoResource = new UrlResource(videoUrl);
            return new Media(MimeType.valueOf(contentType), videoResource);
        } catch (Exception e) {
            throw new VideoProcessingException("Failed to download or process video from URL: " + videoUrl, e);
        }
    }

    /**
     * Helper to decode a Base64 string into a Media object.
     */
    private Media convertBase64ToMedia(Base64Video base64Video) {
        if (!StringUtils.hasText(base64Video.mimeType()) || !StringUtils.hasText(base64Video.data())) {
            throw new VideoProcessingException("Base64 video data and MIME type cannot be empty.");
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Video.data());
            Resource resource = new ByteArrayResource(decodedBytes);
            return new Media(MimeType.valueOf(base64Video.mimeType()), resource);
        } catch (Exception e) {
            throw new VideoProcessingException("Invalid Base64 data provided.", e);
        }
    }

    /**
     * Validates that the user prompt is not empty.
     */
    private void validatePrompt(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new VideoProcessingException("Prompt cannot be empty.");
        }
    }

    /**
     * System prompt that defines the AI's behavior and boundaries for video tasks.
     */
    private static String getSystemPrompt() {
        return """
                You are an AI assistant that specializes in video analysis.
                Your task is to analyze the provided video file(s) and answer the user's question.
                Common tasks are describing scenes, identifying objects, or summarizing the content.
                If the user's prompt is not related to analyzing the video,
                respond with the exact phrase: 'Error: I can only analyze video and answer related questions.'
                """;
    }

    /**
     * Determines the MimeType from a content type string for various video formats.
     */
    private MimeType determineVideoMimeType(String contentType) {
        if (contentType == null) {
            return MimeType.valueOf("video/mp4"); // Default fallback
        }

        return switch (contentType.toLowerCase()) {
            case "video/webm" -> MimeType.valueOf("video/webm");
            case "video/quicktime", "video/mov" -> MimeType.valueOf("video/quicktime");
            case "video/x-ms-wmv", "video/wmv" -> MimeType.valueOf("video/x-ms-wmv"); // WMV
            case "video/x-msvideo", "video/avi" -> MimeType.valueOf("video/x-msvideo"); // AVI
            case "video/x-flv", "video/flv" -> MimeType.valueOf("video/x-flv"); // FLV
            case "video/x-matroska", "video/mkv" -> MimeType.valueOf("video/x-matroska"); // MKV
            case "video/mp2t", "video/mts" -> MimeType.valueOf("video/mp2t"); // MTS
            case "video/x-avchd", "video/avchd" -> MimeType.valueOf("video/mp2t"); // AVCHD (uses same as MTS)
            default -> MimeType.valueOf("video/mp4");
        };
    }
}