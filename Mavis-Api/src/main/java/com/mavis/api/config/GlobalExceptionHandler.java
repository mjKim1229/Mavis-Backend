package com.mavis.api.config;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.dto.ErrorResponse;
import com.mavis.common.exception.GlobalErrorCode;
import com.mavis.common.exception.MavisCodeException;
import com.mavis.infrastructure.outer.discord.DiscordNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final DiscordNotificationService discordNotificationService;

    @ExceptionHandler(exception = MavisCodeException.class)
    public ResponseEntity<ErrorResponse> handleMavisCodeException(
            MavisCodeException e, HttpServletRequest request
    ) {
        ErrorReason errorReason = e.getErrorReason();
        if (errorReason.status() >= 500) {
            log.error("MavisCodeException 5xx [{}]", request.getRequestURI(), e);
            discordNotificationService.sendErrorNotification(e, request.getRequestURI(), request.getMethod());
        }
        ErrorResponse errorResponse = new ErrorResponse(errorReason, request.getRequestURI());
        return ResponseEntity.status(
                        HttpStatusCode.valueOf(errorReason.status())
                )
                .body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("요청 데이터가 유효하지 않습니다.");

        ErrorReason errorReason = ErrorReason.builder()
                .status(400)
                .code("VALIDATION_400_1")
                .reason(errorMessage)
                .build();

        ErrorResponse errorResponse = new ErrorResponse(errorReason, request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(exception = Exception.class)
    public ResponseEntity<Object> handleAllException(Exception e, HttpServletRequest request) {
        GlobalErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        log.error("INTERNAL_SERVER_ERROR", e);
        //discordNotificationService.sendErrorNotification(e, request.getRequestURI(), request.getMethod());
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorCode.getErrorReason());
    }
}
