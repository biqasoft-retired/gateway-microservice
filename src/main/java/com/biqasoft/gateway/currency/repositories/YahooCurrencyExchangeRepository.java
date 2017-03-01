/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.repositories;

import com.biqasoft.gateway.currency.dto.yahoocurrency.YahooCurrencyResources;
import com.biqasoft.gateway.currency.dto.yahoocurrency.YahooCurrencyRootData;
import com.biqasoft.gateway.currency.dto.yahoocurrency.csv.YahooCurrencyCSVRootData;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.biqasoft.microservice.communicator.http.HttpClientsHelpers;

import java.net.URI;

@Service
public class YahooCurrencyExchangeRepository {

    public YahooCurrencyResources getAllCurriencies() {
        String url = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20xml%20where%20url%3D%27http%3A%2F%2Ffinance.yahoo.com%2Fwebservice%2Fv1%2Fsymbols%2Fallcurrencies%2Fquote%27&format=json";

        RestTemplate restTemplate = HttpClientsHelpers.getRestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        return restTemplate.getForObject(URI.create(url), YahooCurrencyRootData.class).getQuery().getResults().getList().getResources();
    }

    public float fromTo(String from, String to) {
        String url = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20csv%20where%20url%3D%22http%3A%2F%2Fdownload.finance.yahoo.com%2Fd%2Fquotes.csv%3Fe%3D.csv%26f%3Dc4l1%26s%3D" + from + to + "%253DX%252C%22%3B&format=json&diagnostics=true&callback=";

        RestTemplate restTemplate = HttpClientsHelpers.getRestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        return Float.parseFloat(restTemplate.getForObject(URI.create(url), YahooCurrencyCSVRootData.class).getQuery().getResults().getRow().getCol1());
    }

}
