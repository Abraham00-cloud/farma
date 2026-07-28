package com.project.farma.analytics.weeklyBreedStandard.service;

import com.project.farma.farm.model.Farm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AutomatedWeatherService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    // Standard constructor injection combining Beans and application configurations
    public AutomatedWeatherService(
            RestTemplate restTemplate,
            @Value("${weather.api.key}") String apiKey,
            @Value("${weather.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public Map<String, Double> fetchEnvironmentalSnapshot(Farm farm) {
        Map<String, Double> climateSnapshot = new HashMap<>();

        // Secure Agronomic Baseline Fallbacks (Comfort Zone defaults)
        climateSnapshot.put("temperature", 24.0);
        climateSnapshot.put("humidity", 60.0);

        if (farm.getLatitude() == null || farm.getLongitude() == null) {
            log.debug("Farm coordinates unmapped. Falling back to default agronomic climate parameters.");
            return climateSnapshot;
        }

        try {
            // Clean, configurable endpoint assembly
            String endpointUrl = String.format(
                    "%s?lat=%s&lon=%s&appid=%s&units=metric",
                    baseUrl, farm.getLatitude(), farm.getLongitude(), apiKey
            );

            Map<?, ?> weatherData = restTemplate.getForObject(endpointUrl, Map.class);
            if (weatherData != null && weatherData.containsKey("main")) {
                Map<?, ?> mainMetrics = (Map<?, ?>) weatherData.get("main");

                climateSnapshot.put("temperature", ((Number) mainMetrics.get("temp")).doubleValue());
                climateSnapshot.put("humidity", ((Number) mainMetrics.get("humidity")).doubleValue());

                log.info("Geo-fenced climate captured automatically: [Temp: {}°C, Humidity: {}%]",
                        climateSnapshot.get("temperature"), climateSnapshot.get("humidity"));
            }
        } catch (Exception e) {
            log.error("Weather API connection timed out. Fallback baselines applied. Root: {}", e.getMessage());
        }

        return climateSnapshot;
    }
}