package com.mavis.admin.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavis.common.dto.ErrorResponse;
import com.mavis.common.exception.MavisCodeException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (MavisCodeException e) {
			responseToClient(response,
				getErrorResponse(e, request.getRequestURI().toString()));
		}
	}

	private void responseToClient(HttpServletResponse response, ErrorResponse errorResponse)
		throws IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(errorResponse.getStatus());
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
	}

	private ErrorResponse getErrorResponse(MavisCodeException e, String path) {
		return new ErrorResponse(e.getErrorReason(), path);
	}
}
