/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data;

import com.biqasoft.entity.constants.CUSTOM_FIELD_TYPES;
import com.biqasoft.entity.core.objects.CustomField;
import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.biqasoft.microservice.i18n.MessageByLocaleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 5/12/2016.
 * All Rights Reserved
 */
@Service
public class HandlebarsHelpersService {

    private ObjectMapper objectMapper = new ObjectMapper();
    private final MicroserviceUsersRepository microserviceUsersRepository;
    private final MessageByLocaleService messageByLocaleService;

    @Autowired
    public HandlebarsHelpersService(MicroserviceUsersRepository microserviceUsersRepository, MessageByLocaleService messageByLocaleService) {
        this.microserviceUsersRepository = microserviceUsersRepository;
        this.messageByLocaleService = messageByLocaleService;
    }

    protected void processHandlebarsHelpers(Handlebars handlebars) {
        try {
            Resource uri = new ClassPathResource("handlebars/ifCond.js");
            handlebars.registerHelpers(uri.getFilename(), uri.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        handlebars.registerHelper("inc", new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {

                if (context instanceof Integer) {
                    Integer context2 = (Integer) context;
                    context2 += 1;
                    return context2.toString();
                }

                return context.toString();
            }
        });

        handlebars.registerHelper("serialize", new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {
                return objectMapper.writeValueAsString(context);
            }
        });

        handlebars.registerHelper("i18n", new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {
                if (context instanceof String){
                    return messageByLocaleService.getMessage((String) context);
                }
                return "";
            }
        });

        handlebars.registerHelper("processCustomFieldValue", new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {

                if (context instanceof CustomField) {
                    CustomField context2 = (CustomField) context;

                    if (context2.getValue() == null) return null;

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.DATE)) {
                        if (context2.getValue().getDateVal() == null) return "-";
                        return context2.getValue().getDateVal().toString();
                    }

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.STRING)) {
                        if (context2.getValue().getStringVal() == null) return "-";
                        return context2.getValue().getStringVal();
                    }

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.DICTIONARY)) {
                        if (context2.getValue().getDictVal() == null || context2.getValue().getDictVal().getValue() == null)
                            return "-";
                        return context2.getValue().getDictVal().getValue().getName();
                    }

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.INTEGER)) {
                        if (context2.getValue().getIntVal() == null) return "-";
                        return context2.getValue().getIntVal().toString();
                    }

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.DOUBLE)) {
                        if (context2.getValue().getDoubleVal() == null) return "-";
                        return context2.getValue().getDoubleVal().toString();
                    }

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.BOOLEAN)) {
                        String falseVal = "нет";
                        if (context2.getValue().getBoolVal() == null) return falseVal;
                        return context2.getValue().getBoolVal() ? "да" : falseVal;
                    }

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.USER_ACCOUNTS)) {
                        if (context2.getValue().getStringList() == null || context2.getValue().getStringList().size() == 0)
                            return "-";
                        StringBuilder builder = new StringBuilder();

                        List<UserAccount> userAccountList = microserviceUsersRepository.findAllUsers().stream().filter(x -> context2.getValue().getStringList().contains(x.getId()))
                                .collect(Collectors.toList());

                        userAccountList.forEach(x -> {
                            builder
                                    .append(x.getFirstname() != null ? x.getFirstname() : " ").append(" ")
                                    .append(x.getLastname() != null ? x.getLastname() : "  ").append(" ")
                                    .append(x.getUsername()).append(" ; ");
                        });

                        return builder.toString();
                    }

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.STRING_LIST)) {
                        if (context2.getValue().getStringList() == null || context2.getValue().getStringList().size() == 0)
                            return "-";
                        StringBuilder builder = new StringBuilder();

                        context2.getValue().getStringList().forEach(x -> {
                            builder.append(x).append(" ; ");
                        });

                        return builder.toString();
                    }

                    if (context2.getType().equals(CUSTOM_FIELD_TYPES.DOCUMENT_FILE)) {
                        if (context2.getValue().getStringList() == null || context2.getValue().getStringList().size() == 0)
                            return "-";
                        StringBuilder builder = new StringBuilder();
                        builder.append("файлы с Id: ");

                        context2.getValue().getStringList().forEach(x -> {
                            builder.append(x).append(" ; ");
                        });

                        return builder.toString();
                    }

                }

                return null;
            }
        });
    }
}
