/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.widgets;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.widgets.Widget;
import com.biqasoft.entity.widgets.WidgetsDashboard;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "Dashboards of widgets")
@Secured(value = {SYSTEM_ROLES.WIDGET_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/widgets/dashboard")
public class WidgetDashboardController {

    private final WidgetRepository widgetRepository;

    @Autowired
    public WidgetDashboardController(WidgetRepository widgetRepository) {
        this.widgetRepository = widgetRepository;
    }

    @Secured(value = {SYSTEM_ROLES.WIDGET_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all dashboards")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<WidgetsDashboard> getAllWidget() {
        List<WidgetsDashboard> widgets = widgetRepository.findAllDashboardWidgets();
        return widgetRepository.resolveWidgets(widgets);
    }

    @Secured(value = {SYSTEM_ROLES.WIDGET_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get one dashboard by id")
    @RequestMapping(value = "id/{id}", method = RequestMethod.GET)
    public WidgetsDashboard getDashboardById(@PathVariable("id") String id) {
        WidgetsDashboard dashboard = widgetRepository.findAllWidgetsFromDashboard(id);

        for (Widget widget : dashboard.getWidgets()) {
            widgetRepository.parseTemplateWidget(widget);
        }

        return dashboard;
    }

    @Secured(value = {SYSTEM_ROLES.WIDGET_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update all dashboards")
    @RequestMapping(value = "all", method = RequestMethod.PUT)
    public  List<WidgetsDashboard> updateAllDashBoards(@RequestBody List<WidgetsDashboard> dashboardWidgetses) {
        return widgetRepository.updateAllDashboard(dashboardWidgetses);
    }

    @Secured(value = {SYSTEM_ROLES.WIDGET_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "add new blank dashboard")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public  WidgetsDashboard addNewDashboard() {
        return widgetRepository.addNewDashboardWidgets(new WidgetsDashboard());
    }

    @Secured(value = {SYSTEM_ROLES.WIDGET_DELETE, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "delete dashboard by id")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteDashboardById(@PathVariable("id") String id) {
        widgetRepository.deleteDashboardById(id);
    }

    @Secured(value = {SYSTEM_ROLES.WIDGET_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update one dashboard")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public  WidgetsDashboard updateOneDashboard(@RequestBody WidgetsDashboard dashboardWidgetses) {
        return widgetRepository.updateOneDashboard(dashboardWidgetses);
    }

}
