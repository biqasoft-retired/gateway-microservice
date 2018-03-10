/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.widgets;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.widgets.Widget;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "Widgets", description = "this is widgets, which is put to dashboards")
@Secured(value = {SystemRoles.WIDGET_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/widgets")
public class WidgetController {

    private final WidgetRepository widgetRepository;

    @Autowired
    public WidgetController(WidgetRepository widgetRepository) {
        this.widgetRepository = widgetRepository;
    }

    @Secured(value = {SystemRoles.WIDGET_EDIT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update one widget")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public Widget updateWidget(@RequestBody Widget widget) {
        return widgetRepository.updateWidget(widget);
    }

    @Secured(value = {SystemRoles.WIDGET_DELETE, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "delete widget by id")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteWidgetById(@PathVariable("id") String id) {
        widgetRepository.deleteWidgetById(id);
    }

    @Secured(value = {SystemRoles.WIDGET_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add new widget")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Widget addNewDashboard(@RequestBody Widget widget, HttpServletResponse response) {
        widgetRepository.addWidget(widget);

        response.setStatus(HttpServletResponse.SC_CREATED);
        return widget;
    }

    @Secured(value = {SystemRoles.WIDGET_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get widget by id")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Widget addNewDashboard(@PathVariable("id") String id) {
        return widgetRepository.findWidgetById(id);
    }

    @Secured(value = {SystemRoles.WIDGET_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all background widgets")
    @RequestMapping(value = "background/all", method = RequestMethod.GET)
    public List<Widget> getAllWidgetBackground() {
        return widgetRepository.getAllClientSideBackgroundWidgets();
    }

}
