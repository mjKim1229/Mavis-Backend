package com.mavis.admin.global.config;

import com.mavis.common.dto.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.mavis")
public class SuccessResponseAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (isBinaryResponse(selectedContentType)) {
            return body;
        }

        HttpServletResponse servletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();
        HttpStatus resolve = HttpStatus.resolve(status);

        if (resolve.is2xxSuccessful()) {
            return new SuccessResponse(body);
        }
        return body;
    }

    private boolean isBinaryResponse(MediaType mediaType) {
        if (mediaType == null) return false;

        return MediaType.APPLICATION_OCTET_STREAM.includes(mediaType)
                || mediaType.toString()
                .contains("application/vnd.openxmlformats-officedocument");
    }
}
