package com.mymicroservice.userservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Getting the requestId from the header or generating a new one
        String requestId = Optional.ofNullable(request.getHeader("X-Request-Id"))
                .orElse(UUID.randomUUID().toString());

        // 2. Putting it in the MDC
        MDC.put(REQUEST_ID, requestId);
        MDC.put("serviceName", serviceName);

        // 3. Adding a header to the response
        response.setHeader("X-Request-Id", requestId);

        // Write a log to the trace file at the beginning of the request
        log.info("{} {}",
                request.getMethod(),
                request.getRequestURI());
        try {
            // 4. We continue the filters
            filterChain.doFilter(request, response);
        } finally {
            // Write the RESPONSE log to the trace file
            log.info("Response status: {}", response.getStatus());

            // 6. Cleaning up the MDC
            MDC.clear();
        }
    }
}
