package com.brianeno.athenasample.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "athena")
public class AppConfiguration {
    private String region;
    private String workGroup;
    private String catalog;
    private String database;
    private String resultsBucket;
    private int clientExecutionTimeout;
    private int limit;
    private int retrySleep;
}