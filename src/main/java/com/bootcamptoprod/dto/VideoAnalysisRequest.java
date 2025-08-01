package com.bootcamptoprod.dto;

import java.util.List;

/**
 * Defines the API request body for analyzing video from URLs or a single classpath file.
 */
public record VideoAnalysisRequest(
        List<String> videoUrls,  // List of video URLs (can be null for classpath)
        String prompt,           // User's question about the video(s)
        String fileName          // Filename for classpath videos (can be null for URLs)
) {
}