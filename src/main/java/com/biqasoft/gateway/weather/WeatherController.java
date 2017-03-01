/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather;

import com.biqasoft.gateway.weather.dto.YahooWeatherQuery;
import com.biqasoft.gateway.weather.repositories.YahooWeatherRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

@Api(value = "Weather", hidden = true)
@ApiIgnore
@RestController
@RequestMapping(value = "/v1/weather")
public class WeatherController {

    private final YahooWeatherRepository yahooWeatherRepository;

    @Autowired
    public WeatherController(YahooWeatherRepository yahooWeatherRepository) {
        this.yahooWeatherRepository = yahooWeatherRepository;
    }

    @ApiOperation(value = "get weather by yahoo place id")
    @RequestMapping(value = "place/{tokenId}", method = RequestMethod.GET)
    public  YahooWeatherQuery getWeatherByPlaceId(HttpServletResponse response, @PathVariable("tokenId") String tokenId) {
        response.setStatus(HttpServletResponse.SC_OK);
        return yahooWeatherRepository.getWeatherByPlaceId(tokenId).getQuery();
    }

}
