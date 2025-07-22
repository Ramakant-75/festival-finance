//package com.example.societyfest.exception;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.stream.Collectors;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
//        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
//        String errorMsg = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(err -> err.getField() + ": " + err.getDefaultMessage())
//                .collect(Collectors.joining(", "));
//        return buildResponse(HttpStatus.BAD_REQUEST, errorMsg, request);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiErrorResponse> handleGeneralError(Exception ex, HttpServletRequest request) {
//        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request);
//    }
//
//    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
//        ApiErrorResponse response = ApiErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(status.value())
//                .error(status.getReasonPhrase())
//                .message(message)
//                .path(request.getRequestURI())
//                .build();
//        return new ResponseEntity<>(response, status);
//    }
//}
//
