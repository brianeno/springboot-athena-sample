package com.brianeno.athenasample.athena;


import com.brianeno.athenasample.config.AppConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.AthenaException;
import software.amazon.awssdk.services.athena.model.GetQueryExecutionRequest;
import software.amazon.awssdk.services.athena.model.GetQueryExecutionResponse;
import software.amazon.awssdk.services.athena.model.QueryExecutionContext;
import software.amazon.awssdk.services.athena.model.QueryExecutionState;
import software.amazon.awssdk.services.athena.model.ResultConfiguration;
import software.amazon.awssdk.services.athena.model.StartQueryExecutionRequest;
import software.amazon.awssdk.services.athena.model.StartQueryExecutionResponse;

@Slf4j
@Component
public class AthenaQuery {

    private final AppConfiguration appConfiguration;

    @Autowired
    public AthenaQuery(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    public String createAthenaQuery(AthenaClient athenaClient, String query) {

        try {
            QueryExecutionContext queryExecutionContext = QueryExecutionContext.builder()
                .database(appConfiguration.getDatabase())
                .build();

            ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                .outputLocation(appConfiguration.getResultsBucket())
                .build();

            StartQueryExecutionRequest startQueryExecutionRequest = StartQueryExecutionRequest.builder()
                .queryString(query)
                .queryExecutionContext(queryExecutionContext)
                .resultConfiguration(resultConfiguration)
                .build();

            StartQueryExecutionResponse startQueryExecutionResponse = athenaClient.startQueryExecution(startQueryExecutionRequest);
            return startQueryExecutionResponse.queryExecutionId();
        } catch (AthenaException e) {
            log.error("Error during query execution", e);
        }
        return "";
    }

    public void completeAthenaQuery(AthenaClient athenaClient, String queryExecutionId) {

        GetQueryExecutionRequest getQueryExecutionRequest = GetQueryExecutionRequest.builder()
            .queryExecutionId(queryExecutionId).build();

        GetQueryExecutionResponse getQueryExecutionResponse;
        boolean isStillRunning = true;
        while (isStillRunning) {
            getQueryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest);
            String queryState = getQueryExecutionResponse.queryExecution().status().state().toString();
            if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("The Amazon Athena query failed to run with error message: " + getQueryExecutionResponse
                    .queryExecution().status().stateChangeReason());
            } else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                throw new RuntimeException("The Amazon Athena query was cancelled.");
            } else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isStillRunning = false;
            } else {
                try {
                    Thread.sleep(appConfiguration.getRetrySleep());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.debug("The current status is: " + queryState);
        }
    }
}