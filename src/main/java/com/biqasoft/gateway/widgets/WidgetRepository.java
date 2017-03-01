/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.widgets;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.WIDGET_LOCATION;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.widgets.Widget;
import com.biqasoft.entity.widgets.WidgetsDashboard;
import com.biqasoft.gateway.datasources.repositories.DataSourceRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class WidgetRepository {

    private final MongoOperations ops;
    private final CurrentUser currentUser;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final DataSourceRepository dataSourceAllData;

    private final static String ID_REPLACED_STRING_IN_HTML_TEMPLATE = "[[[id]]]";

    @Autowired
    public WidgetRepository(@TenantDatabase MongoOperations ops, CurrentUser currentUser, BiqaObjectFilterService biqaObjectFilterService, DataSourceRepository dataSourceAllData) {
        this.ops = ops;
        this.currentUser = currentUser;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.dataSourceAllData = dataSourceAllData;
    }

    public Widget parseTemplateWidget(Widget widget) {
        if (widget.getJsExec() != null)
            widget.setJsExec(widget.getJsExec().replace(ID_REPLACED_STRING_IN_HTML_TEMPLATE, widget.getId()));

        if (widget.getHtmlTemplate() != null)
            widget.setHtmlTemplate(widget.getHtmlTemplate().replace(ID_REPLACED_STRING_IN_HTML_TEMPLATE, widget.getId()));

        return widget;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public void addWidget(Widget widget) {
        if (StringUtils.isEmpty(widget.getName())) {
            widget.setName(widget.getId());
        }

        widget.setOwnerId(currentUser.getCurrentUser().getId());
        ops.insert(widget);
    }

    @BiqaAuditObject
    public Widget updateWidget(Widget widget) {
        widget = biqaObjectFilterService.safeUpdate(widget, ops);
        dataSourceAllData.getActualWidgetWithDataSource(widget);
        return widget;
    }

    public List<Widget> findAllWidgets() {
        return ops.find(Query.query(Criteria.where("ownerId").is(currentUser.getCurrentUser().getId())), Widget.class);
    }

    public Widget findWidgetById(String id) {
        Widget widget = ops.findOne(Query.query(Criteria.where("id").is(id)), Widget.class);
        dataSourceAllData.getActualWidgetWithDataSource(widget);
        return widget;
    }

    public List<WidgetsDashboard> resolveWidgets(List<WidgetsDashboard> widgetsDashboards) {
        for (WidgetsDashboard dashboardWidgets : widgetsDashboards) {
            for (Widget widget : dashboardWidgets.getWidgets()) {

                dataSourceAllData.getActualWidgetWithDataSource(widget);

                this.parseTemplateWidget(widget);
            }
        }
        return widgetsDashboards;
    }

    public List<Widget> getAllClientSideBackgroundWidgets() {
        List<Widget> widgets = ops.find(Query.query(Criteria.where("location").is(WIDGET_LOCATION.BACKGROUND)), Widget.class);
        widgets.forEach(dataSourceAllData::getActualWidgetWithDataSource);
        return widgets;
    }

    public WidgetsDashboard findAllWidgetsFromDashboard(String id) {
        WidgetsDashboard widgetsDashboard = ops.findOne(Query.query(Criteria.where("id").is(id)), WidgetsDashboard.class);

        widgetsDashboard.getWidgets().forEach(dataSourceAllData::getActualWidgetWithDataSource);
        return widgetsDashboard;
    }

    public boolean deleteDashboardById(String id) {
        WidgetsDashboard dashboardWidgets = findAllWidgetsFromDashboard(id);
        ops.remove(dashboardWidgets);
        return true;
    }

    public boolean deleteWidgetById(String id) {
        Widget widget = findWidgetById(id);

        WidgetsDashboard dashboardWidgetsToDelete = null;
        Widget widgetToDelete = null;
        List<WidgetsDashboard> allDashboards = findAllDashboardWidgets();

        for (WidgetsDashboard dashboardWidgets : allDashboards) {
            for (Widget currentWidget : dashboardWidgets.getWidgets()) {

                if (currentWidget.equals(widget)) {
                    dashboardWidgetsToDelete = dashboardWidgets;
                    widgetToDelete = currentWidget;
                }
            }
        }

        if (dashboardWidgetsToDelete != null) {
            dashboardWidgetsToDelete.getWidgets().remove(widgetToDelete);
        }

        updateOneDashboard(dashboardWidgetsToDelete);

        ops.remove(widget);
        return true;
    }

    // TODO: looks like bad method for optimistic lock
    public List<WidgetsDashboard> updateAllDashboard(List<WidgetsDashboard> widgetsDashboards) {

        for (WidgetsDashboard dashboardWidgets : widgetsDashboards) {
            dashboardWidgets.getWidgets().forEach(this::updateWidget);
        }

        for (WidgetsDashboard dashboardWidgets : widgetsDashboards) {
             biqaObjectFilterService.safeUpdate(dashboardWidgets, ops);
            dashboardWidgets.getWidgets().forEach(dataSourceAllData::getActualWidgetWithDataSource);
        }

        return widgetsDashboards;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public WidgetsDashboard addNewDashboardWidgets(WidgetsDashboard dashboardWidgets) {
        dashboardWidgets.setOwnerId(currentUser.getCurrentUser().getId());

        if (StringUtils.isEmpty(dashboardWidgets.getName())) {
            dashboardWidgets.setName(Integer.toString(findAllDashboardWidgets().size() + 1));
        }

        ops.insert(dashboardWidgets);

        dashboardWidgets.getWidgets().forEach(dataSourceAllData::getActualWidgetWithDataSource);
        return dashboardWidgets;
    }

//    private Widget getLastWidget(List<Widget> widgets) {
//
//        Widget lastWidget = widgets.get(0);
//        int lastWidgetPosition = lastWidget.getRow() + lastWidget.getSizeY();
//
//        for (Widget widget : widgets) {
//            if (widget.getRow() + widget.getSizeY() > lastWidgetPosition) {
//                lastWidget = widget;
//            }
//        }
//
//        return lastWidget;
//    }

    private void normalizeListOfWidgetsColumnAndRow(List<Widget> widgets) {

//        if (widgets.size() == 0 ) return;
//
//        for (int i=0; i < widgets.size(); i++){
//            for (Widget widget : widgets) {
//                if (widget.getRow() == 0){
//                    widget.setRow(getLastWidget(widgets).getRow() + 3);
//                }
//            }
//        }
//
    }

    @BiqaAuditObject
    public WidgetsDashboard updateOneDashboard(WidgetsDashboard dashboardWidgetses) {
        int maxRow = 0;

        WidgetsDashboard curentDW = findAllWidgetsFromDashboard(dashboardWidgetses.getId());
        normalizeListOfWidgetsColumnAndRow(curentDW.getWidgets());

        dashboardWidgetses = biqaObjectFilterService.safeUpdate(dashboardWidgetses, ops);
        dashboardWidgetses.getWidgets().forEach(x -> dataSourceAllData.getActualWidgetWithDataSource(x));
        return dashboardWidgetses;
    }

    public List<WidgetsDashboard> findAllDashboardWidgets() {
        List<WidgetsDashboard> widgets = ops.find(Query.query(Criteria.where("ownerId").is(currentUser.getCurrentUser().getId())), WidgetsDashboard.class);

        widgets.forEach(p -> p.getWidgets().forEach(x -> dataSourceAllData.getActualWidgetWithDataSource(x)));
        return widgets;
    }

}
