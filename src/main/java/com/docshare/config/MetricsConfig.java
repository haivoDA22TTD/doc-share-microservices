package com.docshare.config;

import com.docshare.service.MetricsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {
    
    private final MetricsService metricsService;
    
    @PostConstruct
    public void init() {
        metricsService.init();
    }
}
