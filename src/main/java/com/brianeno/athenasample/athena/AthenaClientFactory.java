package com.brianeno.athenasample.athena;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.athena.AthenaClient;

@Component
public class AthenaClientFactory {

    public AthenaClient createAthenaClient() {
        return AthenaClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            //.credentialsProvider(EnvironmentVariableCredentialsProvider.create()) // use if creds are
            // env vars
            .build();
    }
}