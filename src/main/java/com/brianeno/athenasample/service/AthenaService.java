package com.brianeno.athenasample.service;

import com.brianeno.athenasample.athena.AthenaClientFactory;
import com.brianeno.athenasample.athena.AthenaQuery;
import com.brianeno.athenasample.model.TopTenCities;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.ColumnInfo;
import software.amazon.awssdk.services.athena.model.Datum;
import software.amazon.awssdk.services.athena.model.GetQueryResultsRequest;
import software.amazon.awssdk.services.athena.model.GetQueryResultsResponse;
import software.amazon.awssdk.services.athena.model.Row;
import software.amazon.awssdk.services.athena.paginators.GetQueryResultsIterable;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AthenaService {

    private static final String QUERY_ALL = "SELECT city, population FROM worldcities " +
        "ORDER BY population DESC LIMIT {limit}";

    private static final String QUERY = "SELECT city, population FROM worldcities where iso2 = '{country}' " +
        "ORDER BY population DESC LIMIT {limit}";

    private final AthenaClientFactory athenaClientFactory;

    private final AthenaQuery athenaQuery;

    public List<TopTenCities> getTop10All(Integer limit) {
        return run("", limit, QUERY_ALL);
    }

    public List<TopTenCities> getTop10(String country, Integer limit) {
        return run(country, limit, QUERY);
    }

    public List<TopTenCities> run(String country, Integer limit, String query) {

        AthenaClient athenaClient = athenaClientFactory.createAthenaClient();

        String finalQuery = query.replace("{country}", country)
            .replace("{limit}", String.valueOf(limit));
        String queryExecutionId = athenaQuery.createAthenaQuery(athenaClient,
            finalQuery);

        log.info("Query submitted with query id {} and current mills {}", queryExecutionId, System.currentTimeMillis());

        athenaQuery.completeAthenaQuery(athenaClient, queryExecutionId);

        log.info("Query finished at {}", System.currentTimeMillis());

        return processResultRows(athenaClient, queryExecutionId);
    }

    private List<TopTenCities> processResultRows(AthenaClient athenaClient, String queryExecutionId) {

        List<TopTenCities> result = new ArrayList<>();
        GetQueryResultsRequest getQueryResultsRequest = GetQueryResultsRequest.builder()
            .queryExecutionId(queryExecutionId).build();

        GetQueryResultsIterable getQueryResultsResults = athenaClient.getQueryResultsPaginator(getQueryResultsRequest);

        for (GetQueryResultsResponse Resultresponse : getQueryResultsResults) {
            List<ColumnInfo> columnInfoList = Resultresponse.resultSet().resultSetMetadata().columnInfo();

            int resultSize = Resultresponse.resultSet().rows().size();
            log.info("Result size: " + resultSize);

            List<Row> results = Resultresponse.resultSet().rows();
            result = processRow(results, columnInfoList);
        }
        return result;
    }

    private List<TopTenCities> processRow(List<Row> rowList, List<ColumnInfo> columnInfoList) {

        List<String> columns = new ArrayList<>();
        List<TopTenCities> result = new ArrayList<>();

        for (ColumnInfo columnInfo : columnInfoList) {
            columns.add(columnInfo.name());
        }

        int rowCtr = 0;
        for (Row row : rowList) {
            int index = 0;

            // simple mapping logic for the POJO
            String name = "";
            String population = "";

            for (Datum datum : row.data()) {
                log.debug(columns.get(index) + ": " + datum.varCharValue());
                // skip row header row
                if (rowCtr > 0) {
                    if (index == 0) {
                        name = datum.varCharValue();
                    } else {
                        population = datum.varCharValue();
                    }
                }
                index++;
            }
            rowCtr++;
            if (!name.isEmpty()) {
                var topTenCities = new TopTenCities(name, Integer.valueOf(population));
                result.add(topTenCities);
            }
            log.debug("===================================");
        }
        return result;
    }
}
