/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.search;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.gateway.search.dto.SearchRequest;
import com.biqasoft.gateway.search.dto.SearchResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Search")
@Secured(value = {SYSTEM_ROLES.SEARCH_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/search")
public class SearchController {

    private final SearchRepository searchRepository;

    @Autowired
    public SearchController(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @ApiOperation(value = "search everywhere")
    @Secured(value = {SYSTEM_ROLES.SEARCH_EVERYWHERE, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(value = "", method = RequestMethod.POST)
    public SearchResult searchEverywhere(@RequestBody SearchRequest searchRequest) {
        return searchRepository.searchAll(searchRequest);
    }

}
