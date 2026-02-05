package com.mymicroservice.userservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String SERVICE_NAME = "userservice";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Получаем requestId из заголовка или генерируем новый
        String requestId = Optional.ofNullable(request.getHeader("X-Request-Id"))
                .orElse(UUID.randomUUID().toString());

        // 2. Кладём в MDC
        MDC.put(REQUEST_ID, requestId);
        MDC.put("serviceName", SERVICE_NAME);

        // 3. Добавляем заголовок в ответ
        response.setHeader("X-Request-Id", requestId);

        // Пишем в файл трассировки лог в начале запроса
        log.info("{} {}",
                request.getMethod(),
                request.getRequestURI());
        try {
            // 4. Продолжаем фильтры
            filterChain.doFilter(request, response);
        } finally {
            // Пишем в файл трассировки лог при RESPONSE
            log.info("Response status: {}", response.getStatus());

            // 6. Очищаем MDC
            MDC.clear();
        }
    }
}
