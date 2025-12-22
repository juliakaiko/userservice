package com.mymicroservice.userservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    // Логгер для MDC
    private static final Logger TRACE_MDC_LOGGER = LoggerFactory.getLogger("TRACE_MDC_LOGGER");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Получаем requestId из заголовка или генерируем новый
        String requestId = Optional.ofNullable(request.getHeader("X-Request-Id"))
                .orElse(UUID.randomUUID().toString());

        // 2. Кладём в MDC
        MDC.put(REQUEST_ID, requestId);
        MDC.put("serviceName", "userservice");

        // 3. Добавляем заголовок в ответ
        response.setHeader("X-Request-Id", requestId);

        // Логируем ВХОДЯЩИЙ запрос (один раз!)
        TRACE_MDC_LOGGER.info("{} {}",
                request.getMethod(),
                request.getRequestURI());
        try {
            // 4. Продолжаем фильтры
            filterChain.doFilter(request, response);
        } finally {
            // Логируем ИСХОДЯЩИЙ ответ
            TRACE_MDC_LOGGER.info("Response status: {}", response.getStatus());

            // 6. Очищаем MDC
            MDC.clear();
        }
    }
}
