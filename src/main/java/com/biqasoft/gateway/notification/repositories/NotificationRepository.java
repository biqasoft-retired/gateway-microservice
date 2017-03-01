/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.notification.repositories;

import com.biqasoft.gateway.notification.dto.NotificationText;
import com.biqasoft.gateway.notification.dto.PushNotificationAccessDAO;
import com.biqasoft.gateway.notification.dto.android.push.AndroidPushData;
import com.biqasoft.gateway.notification.dto.android.push.AndroidPushRoot;
import com.biqasoft.microservice.communicator.http.HttpClientsHelpers;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.microservice.database.TenantDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty("google.android.push")
public class NotificationRepository {

    private static final String HTTPS_ANDROID_GOOGLEAPIS_COM_GCM_SEND = "https://android.googleapis.com/gcm/send";
    private final MongoOperations ops;
    private final CurrentUser currentUser;
    private String googleAndroidPushToken;

    @Autowired
    public NotificationRepository(@Value("${google.android.push}") String googleAndroidPushToken, @TenantDatabase MongoOperations ops, CurrentUser currentUser) {
        this.ops = ops;
        this.currentUser = currentUser;
        this.googleAndroidPushToken = googleAndroidPushToken;
    }

    public void addPushNotificationAccess(PushNotificationAccessDAO note) {
        ops.insert(note);
    }

    public PushNotificationAccessDAO findPushNotificationAccessDAOByDeviceId(String id) {
        return ops.findOne(Query.query(Criteria.where("uuid").is(id)), PushNotificationAccessDAO.class);
    }

    public void deleteNotificationAccessByDeviceId(String id) {
        PushNotificationAccessDAO de = findPushNotificationAccessDAOByDeviceId(id);
        ops.remove(de);
    }

    public PushNotificationAccessDAO updatePushNotificationAccess(PushNotificationAccessDAO note) {
        ops.save(note);
        return note;
    }

    public PushNotificationAccessDAO findPushNotificationAccessDAOById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), PushNotificationAccessDAO.class);
    }

    public void sendPushToDeviceWithPushAccessAndMessage(PushNotificationAccessDAO pushNotificationAccessDAO, NotificationText notificationText) {

        if (pushNotificationAccessDAO.getPlatform().equals("ANDROID")) {
            AndroidPushRoot androidPushRoot = new AndroidPushRoot();
            AndroidPushData androidPushData = new AndroidPushData();

            androidPushRoot.setData(androidPushData);

            List<String> allIds = new ArrayList<>();
            allIds.add(pushNotificationAccessDAO.getClientId());
            androidPushRoot.setRegistration_ids(allIds);

            androidPushData.setMessage(notificationText.getShortText());

            RestTemplate restTemplate = HttpClientsHelpers.getRestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Authorization", googleAndroidPushToken);

            HttpEntity<AndroidPushRoot> request = new HttpEntity<>(androidPushRoot, headers);

            AndroidPushData re = restTemplate.postForObject(HTTPS_ANDROID_GOOGLEAPIS_COM_GCM_SEND, request, AndroidPushData.class);
        }

    }

    public List<PushNotificationAccessDAO> findAllPushNotificationAccessOnlyMyANDROID() {
        return ops.find(Query.query(Criteria.where("useraccount").is(currentUser.getCurrentUser())
                .and("platform").is("ANDROID")
        ), PushNotificationAccessDAO.class);
    }

}
