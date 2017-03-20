/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.analytics.controllers;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.analytics.AnalyticRecord;
import com.biqasoft.entity.analytics.UTMAllMetricInfo;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.gateway.analytics.repositories.AnalyticsRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Api(value = "public analytics", description = "public analytics using to add new records from web site (or systems), get new cookies or recognising users")
@RestController
@RequestMapping(value = "/public/analytics/web")
public class WebAnalyticsController {

    private final CurrentUser currentUser;
    private final AnalyticsRepository analyticsRepository;

    @Autowired
    public WebAnalyticsController(CurrentUser currentUser, AnalyticsRepository analyticsRepository) {
        this.currentUser = currentUser;
        this.analyticsRepository = analyticsRepository;
    }

    @ApiOperation(value = "get main counter (pixel) to tracking user", notes = ", you should add this image to any page, when want to tack user actions")
    @RequestMapping(value = "/tp.jpg", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getNewAnalyticRecordImage(HttpServletRequest request,
                                            @RequestParam(value = "utm_source", required = false) String utm_source,
                                            @RequestParam(value = "utm_medium", required = false) String utm_medium,
                                            @RequestParam(value = "utm_campaign", required = false) String utm_campaign,
                                            @RequestParam(value = "utm_content", required = false) String utm_content,
                                            @RequestParam(value = "utm_term", required = false) String utm_term,
                                            @RequestParam(value = "utm_type", required = false) String utm_type,


                                            @RequestParam(value = "counterId", required = false) String counterId,
                                            @RequestParam(value = "userCookieId", required = false) String userCookieId,

                                            @RequestParam(value = "pathname", required = false) String pathname,
                                            @RequestParam(value = "fullUrl", required = false) String fullUrl,
                                            @RequestParam(value = "action", required = false) String action
    ) {

//        {
//            "counterId": "54f31c94534b922ebb640d67",
//                "userCookieId": "54f33b8340535b11df127764",
//                "utm": {
//            "utm_source": "typein",
//                    "utm_medium": "typein",
//                    "utm_campaign": null,
//                    "utm_content": null,
//                    "utm_term": null,
//                    "utm_type": "typein"
//        },
//            "pathname": "/blagoustroystvo.html",
//                "fullUrl": "http://landing-test.dev/blagoustroystvo.html",
//                "action": "pageView",
//                "yandexDirectClick": null
//        }


////////////////////////////////////////////////////////
        if (userCookieId == null) {

            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("userCookieId")) {
                    userCookieId = cookie.getValue();
                }
            }

        }
////////////////////////////////////////////////////////

        if (analyticsRepository.findWebAnalyticsCounter(counterId) == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("analytics.no_such_counter");
        }

        AnalyticRecord analyticRecord = new AnalyticRecord();
        analyticRecord.setAction(action);
        analyticRecord.setCounterId(counterId);
        analyticRecord.setFullUrl(fullUrl);
        analyticRecord.setPathname(pathname);
        analyticRecord.setUserCookieId(userCookieId);

        UTMAllMetricInfo utms = new UTMAllMetricInfo();
        utms.setUtm_medium(utm_medium);
        utms.setUtm_campaign(utm_campaign);
        utms.setUtm_source(utm_source);
        utms.setUtm_content(utm_content);
        utms.setUtm_term(utm_term);

        analyticRecord.setUtm(utms);

        analyticRecord.setDomain(analyticsRepository.findWebAnalyticsCounter(analyticRecord.getCounterId()).getDomain());

        CreatedInfo createdInfo = new CreatedInfo(new Date(), currentUser.getCurrentUser().getId());
        analyticRecord.setCreatedInfo(createdInfo);

        analyticsRepository.addAnalyticRecord(analyticRecord);

        return new byte[0];
    }

    @ApiOperation(value = "add new record (pageView, transactions, conversions and other actions)", notes = "one of the mose used endpoint, where you pass user actions")
    @RequestMapping(value = "/record", method = RequestMethod.POST)
    public AnalyticRecord addNewAnalyticRecord(@RequestBody AnalyticRecord analyticRecord, HttpServletResponse response) {

        if (analyticsRepository.findWebAnalyticsCounter(analyticRecord.getCounterId()) == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("analytics.no_such_counter");
        }

        analyticRecord.setDomain(analyticsRepository.findWebAnalyticsCounter(analyticRecord.getCounterId()).getDomain());

        CreatedInfo createdInfo = new CreatedInfo();
        createdInfo.setCreatedDate(new Date());

        analyticRecord.setCreatedInfo(createdInfo);

        analyticsRepository.addAnalyticRecord(analyticRecord);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return analyticRecord;
    }

}
