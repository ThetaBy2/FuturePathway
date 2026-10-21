package com.futurepathway.exception;

/** Gemini call failed and no fallback could produce the requested result. */
public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
