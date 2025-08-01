package com.bootcamptoprod.dto;

/**
 * Represents the final text response from the AI model after video analysis,
 * which is sent back to the client. This DTO is used for all successful API responses.
 */
public record VideoAnalysisResponse(
        String response  // AI-generated analysis text
) {
}