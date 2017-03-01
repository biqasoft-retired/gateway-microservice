/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.email.services;

import com.biqasoft.notifications.email.Email;
import com.biqasoft.notifications.email.EmailSenderProvider;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.biqasoft.gateway.useraccount.dto.ResetPasswordTokenDTO;
import com.biqasoft.entity.tasks.Task;
import com.biqasoft.entity.core.useraccount.UserAccount;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Prepare email templates  with {@code https://github.com/jknack/handlebars.java}
 * and send with {@link EmailSenderProvider}
 */
@Service
public class EmailPrepareAndSendService {

    private final Handlebars handlebars;
    private final Map<String, Template> compiledTemplates;
    private final EmailSenderProvider emailSenderProvider;
    private String baseCloudUrl;
    private String biqaSupportEmail;
    private String biqaSupportUrl;
    private String senderEmail;
    private String systemName;

    @Autowired
    public EmailPrepareAndSendService(EmailSenderProvider emailSenderProvider,
                                      @Value("${biqa.urls.http.cloud}") String baseCloudUrl,
                                      @Value("${biqa.urls.http.cloud}") String biqaSupportEmail,
                                      @Value("${biqa.urls.http.support}") String biqaSupportUrl,
                                      @Value("${biqa.notification.email.sender.email}") String senderEmail,
                                      @Value("${biqa.notification.header.system}") String systemName
    ) {
        this.handlebars = new Handlebars();
        this.compiledTemplates = new HashMap<>();
        this.emailSenderProvider = emailSenderProvider;
        this.baseCloudUrl = baseCloudUrl;
        this.biqaSupportEmail = biqaSupportEmail;
        this.biqaSupportUrl = biqaSupportUrl;
        this.senderEmail = senderEmail;
        this.systemName = systemName;

        try {
            compiledTemplates.put("task_done", handlebars.compile("templates/task_done"));
            compiledTemplates.put("new_task_to_responsible", handlebars.compile("templates/new_task_to_responsible"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public void sendTaskDoneEmail(UserAccount userAccount, Task task) {
        Template template = compiledTemplates.get("task_done");

        Map<String, Object> map = new HashMap<>();
        map.put("task", task);
        addBaseParamsToMap(map);

        String emailText = processTemplateWithContext(template, createContextFromMap(map));
        Email email = new Email(senderEmail, userAccount.getEmail(), emailText, "Задача " + task.getName() + " в CRM выполнена");

        emailSenderProvider.sendEmail(email);
    }

    public void addNewTaskEmailSendResponsible(UserAccount userAccount, Task task) {
        Template template = compiledTemplates.get("new_task_to_responsible");

        Map<String, Object> map = new HashMap<>();
        map.put("task", task);
        addBaseParamsToMap(map);

        String emailText = processTemplateWithContext(template, createContextFromMap(map));
        Email email = new Email(senderEmail, userAccount.getEmail(), emailText, "Новая задача " + task.getName() + " в " + systemName);

        emailSenderProvider.sendEmail(email);
    }

    private Context createContextFromMap(Map<String, Object> map) {
        Context context = Context
                .newBuilder(map)
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE)
                .build();
        return context;
    }

    private String processTemplateWithContext(Template template, Context context) {
        String emailText;
        try {
            emailText = template.apply(context);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return emailText;
    }

    private void addBaseParamsToMap(Map<String, Object> map) {
        map.put("baseCloudUrl", baseCloudUrl);
        map.put("biqaSupportEmail", biqaSupportEmail);
        map.put("biqaSupportUrl", biqaSupportUrl);
    }

}
