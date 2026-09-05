package com.example.societyfest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured error response DTO that allows highlighting specific parts of the error message.
 * This enables the UI to display the main error message prominently and additional context separately.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Main error message - this should be highlighted/emphasized on UI
     */
    private String mainMessage;
    
    /**
     * Additional context or sarcastic message (optional)
     */
    private String additionalMessage;
    
    /**
     * Error code for programmatic handling
     */
    private String errorCode;
    
    /**
     * HTTP Status code
     */
    private int status;
}

