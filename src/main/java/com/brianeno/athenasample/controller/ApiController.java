package com.brianeno.athenasample.controller;

import com.brianeno.athenasample.model.TopTenCities;
import com.brianeno.athenasample.service.AthenaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/cities", produces = APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiController {

    private final AthenaService athenaService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<TopTenCities>> getTop10All(@RequestParam(defaultValue = "10") Integer limit) {

        List<TopTenCities> result = athenaService.getTop10All(limit);
        return ResponseEntity.accepted().body(result);
    }

    @GetMapping(value = {"/{country}"})
    public ResponseEntity<List<TopTenCities>> getTop10(@PathVariable("country") String country,
                                                       @RequestParam(defaultValue = "10") Integer limit) {

        List<TopTenCities> result = athenaService.getTop10(country, limit);
        return ResponseEntity.accepted().body(result);
    }
}
