package com.driveeasy.client.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** Same JSON error format for every error of the API. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details   // invalid fields (validation errors), else null
) {}
