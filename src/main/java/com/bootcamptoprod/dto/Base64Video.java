package com.bootcamptoprod.dto;

/**
 * Represents a single video file encoded as a Base64 string, including its MIME type.
 */
public record Base64Video(
        String mimeType,  // e.g., "video/mp4", "video/webm"
        String data       // Base64 encoded video content
) {
}