package group2026.gismeteo;

import com.fasterxml.jackson.databind.ObjectMapper;
import group2026.weather.WeatherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Slf4j
@Component
@RequiredArgsConstructor
public class GismeteoWeatherClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final GismeteoWeatherProperties properties;
    private final WeatherProperties weatherProperties;


    public GismeteoWeatherResponse getWeather() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl()))
                .header(
                        "X-Gismeteo-Token",
                        properties.apiKey()
                )
                .header(
                        HttpHeaders.ACCEPT,
                        "application/json"
                )
                .GET()
                .build();


        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );


            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Gismeteo status: "
                                + response.statusCode()
                );
            }


            return objectMapper.readValue(
                    response.body(),
                    GismeteoWeatherResponse.class
            );


        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Gismeteo request interrupted",
                    e
            );


        } catch (IOException e) {

            throw new RuntimeException(
                    "Cannot parse Gismeteo response",
                    e
            );
        }
    }


    private String buildUrl() {

        return "%s?latitude=%s&longitude=%s"
                .formatted(
                        properties.apiUrl(),
                        weatherProperties.latitude(),
                        weatherProperties.longitude()
                );
    }
}