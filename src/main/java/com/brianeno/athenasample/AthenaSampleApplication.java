package com.brianeno.athenasample;

import com.brianeno.athenasample.config.AppConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(AppConfiguration.class)
public class AthenaSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AthenaSampleApplication.class, args);
    }
}
