package com.mavis.api.config;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.dto.ErrorResponse;
import com.mavis.common.exception.GlobalErrorCode;
import com.mavis.common.exception.MavisCodeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(exception = MavisCodeException.class)
    public ResponseEntity<ErrorResponse> handleMavisCodeException(
            MavisCodeException e, HttpServletRequest request
    ) {
        ErrorReason errorReason = e.getErrorReason();
        ErrorResponse errorResponse = new ErrorResponse(errorReason, request.getRequestURI());
        return ResponseEntity.status(
                        HttpStatusCode.valueOf(errorReason.status())
                )
                .body(errorResponse);
    }

    @ExceptionHandler(exception = Exception.class)
    public ResponseEntity<Object> handleAllException(Exception e) {
        GlobalErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        log.error("INTERNAL_SERVER_ERROR", e);
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorCode.getErrorReason());
    }
}
