package com.duoc.bff_web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pool de hilos usado para ejecutar en paralelo las llamadas al Backend
 * Central dentro del dashboard, ya que las 3 fuentes (cuenta, transacciones,
 * movimientos) son independientes entre sí.
 */
@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService backendExecutor() {
        return Executors.newFixedThreadPool(3);
    }
}