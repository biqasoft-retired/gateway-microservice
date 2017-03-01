/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.notification.controllers;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.gateway.notification.dto.NotificationText;
import com.biqasoft.gateway.notification.dto.PushNotificationAccessDAO;
import com.biqasoft.gateway.notification.repositories.NotificationRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "Notifications", description = "at pre alpha")
@ApiIgnore
@RestController
@RequestMapping(value = "/v1/notification")
@ConditionalOnProperty("google.android.push")
public class NotificationController {

    private final CurrentUser currentUser;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository, CurrentUser currentUser) {
        this.notificationRepository = notificationRepository;
        this.currentUser = currentUser;
    }

    @ApiOperation(value = "push notification to android")
    @RequestMapping(value = "/push/android", method = RequestMethod.GET)
    public List<PushNotificationAccessDAO> findAllPushNotificationAccessDAOOnlyMy() {
        return notificationRepository.findAllPushNotificationAccessOnlyMyANDROID();
    }

    @RequestMapping(value = "/sendPushToDevice/{id}", method = RequestMethod.POST)
    public NotificationText sendPushToDevice(@RequestBody NotificationText notificationText,  @PathVariable("id") String id) {
        notificationRepository.sendPushToDeviceWithPushAccessAndMessage(notificationRepository.findPushNotificationAccessDAOById(id), notificationText);
        return notificationText;
    }

    @RequestMapping(value = "/push/access", method = RequestMethod.POST)
    public PushNotificationAccessDAO addOrUpdatePushAccess(@RequestBody PushNotificationAccessDAO pushedToken) {

        pushedToken.setUserAccount(currentUser.getCurrentUser());
        pushedToken.setPlatform(pushedToken.getPlatform().toUpperCase());

        PushNotificationAccessDAO pushNotificationAccessDAO = notificationRepository.findPushNotificationAccessDAOByDeviceId(pushedToken.getUuid());
        if (pushNotificationAccessDAO == null) {
            notificationRepository.addPushNotificationAccess(pushedToken);
        } else {
            String oldId = pushNotificationAccessDAO.getId();
            notificationRepository.deleteNotificationAccessByDeviceId(pushedToken.getUuid());
            pushedToken.setId(oldId);
            notificationRepository.updatePushNotificationAccess(pushedToken);

        }

        return pushedToken;

    }

}
