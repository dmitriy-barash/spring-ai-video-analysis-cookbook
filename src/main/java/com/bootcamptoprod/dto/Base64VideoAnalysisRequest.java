package com.bootcamptoprod.dto;

import java.util.List;

/**
 * Defines the API request body for analyzing one or more Base64 encoded video files.
 */
public record Base64VideoAnalysisRequest(
        List<Base64Video> base64VideoList,  // List of Base64 encoded videos
        String prompt                       // Analysis instruction
) {
}