package com.duoc.bff_web.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResilienceEventLogger {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @PostConstruct
    public void registrarListeners() {
        circuitBreakerRegistry.circuitBreaker("bancoCentral").getEventPublisher()
                .onStateTransition(event -> log.warn("CircuitBreaker bancoCentral cambio de estado: {}", event));

        retryRegistry.retry("bancoCentral").getEventPublisher()
                .onRetry(event -> log.warn("Retry bancoCentral intento #{}: {}", event.getNumberOfRetryAttempts(), event));
    }
}