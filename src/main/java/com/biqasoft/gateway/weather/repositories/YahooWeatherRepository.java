/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.repositories;

import com.biqasoft.gateway.weather.dto.YahooWeatherRootData;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class YahooWeatherRepository {

    public YahooWeatherRootData getWeatherByPlaceId(String token) {
        String url = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20xml%20where%20url%3D%27http%3A%2F%2Fweather.yahooapis.com%2Fforecastrss%3Fw%3D" + token + "%26u%3Dc%27&format=json";

        RestTemplate restTemplate = com.biqasoft.microservice.communicator.http.HttpClientsHelpers.getRestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        return restTemplate.getForObject(URI.create(url), YahooWeatherRootData.class);
    }

}
