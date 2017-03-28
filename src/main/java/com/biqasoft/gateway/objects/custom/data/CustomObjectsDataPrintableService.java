/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data;

import com.biqasoft.common.exceptions.InvalidRequestException;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.constants.CUSTOM_OBJECTS_PRINTABLE_TYPES;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.objects.CustomObjectData;
import com.biqasoft.entity.objects.CustomObjectPrintableTemplate;
import com.biqasoft.entity.objects.CustomObjectTemplate;
import com.biqasoft.gateway.export.MicroserviceExport;
import com.biqasoft.gateway.objects.custom.data.dto.PrintableDataContextSaver;
import com.biqasoft.gateway.objects.custom.data.dto.RequestPrintableBuilder;
import com.biqasoft.gateway.objects.custom.template.CustomObjectsRepository;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This is real custom objects
 */
@Service
public class CustomObjectsDataPrintableService {

    private final CustomObjectsRepository customObjectsRepository;
    private final CurrentUser currentUser;
    private final HandlebarsHelpersService handlebarsHelpersService;
    private Handlebars handlebars;
    private static final Logger logger = LoggerFactory.getLogger(CustomObjectsDataPrintableService.class);
    private final MicroserviceExport microserviceExport;

    @Autowired
    public CustomObjectsDataPrintableService(HandlebarsHelpersService handlebarsHelpersService, CurrentUser currentUser, CustomObjectsRepository customObjectsRepository, MicroserviceExport microserviceExport) {
        this.handlebarsHelpersService = handlebarsHelpersService;
        this.currentUser = currentUser;
        this.customObjectsRepository = customObjectsRepository;
        this.microserviceExport = microserviceExport;
    }

    @PostConstruct
    private void init() {
        this.handlebars = new Handlebars();
        handlebarsHelpersService.processHandlebarsHelpers(handlebars);
    }

    public PrintableDataContextSaver getPrintableData(CustomObjectData customObjectData, RequestPrintableBuilder builder) {
        String printObjectId = builder.getViewId();
        byte[] bytes = null;
        PrintableDataContextSaver printableDataContextSaver = new PrintableDataContextSaver();

        CustomObjectTemplate customObjectTemplate = customObjectsRepository.findCustomObjectById(customObjectData.getCollectionId());

        String mimeType = null;
        String extension = null;

        if (builder.getRequestedMimeType() != null && builder.getRequestedMimeType().length() > 0) {
            mimeType = builder.getRequestedMimeType();
        }

        if (builder.getRequestedExtension() != null && builder.getRequestedExtension().length() > 0) {
            extension = builder.getRequestedExtension();
        }

        if (customObjectTemplate == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.print.no_such_custom_object_template");
        }
        if (printObjectId == null || printObjectId.length() == 0) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.print.no_such_printable_view");
        }

        CustomObjectPrintableTemplate printableTemplate = customObjectTemplate.getPrintableTemplates().stream()
                .filter(x -> x != null && x.getId() != null && x.getId().equals(printObjectId))
                .findFirst().get();

        if (printableTemplate == null || printObjectId.length() == 0) {
            ThrowExceptionHelper.throwExceptionInvalidRequest("No such printable view with id " + printObjectId + " for template " + customObjectTemplate.getId());
        }

        if (mimeType == null || extension == null) {
            mimeType = printableTemplate.getMimeType();
            extension = printableTemplate.getExtension();
        }

        printableDataContextSaver.setPrintableTemplate(printableTemplate);

        switch (printableTemplate.getType()) {

            case CUSTOM_OBJECTS_PRINTABLE_TYPES.HANDLEBARS:
                bytes = getHandlebarsBytes(customObjectData, customObjectTemplate, printableTemplate);

                if (bytes.length == 0) {
                    ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.print.empty_template");
                }

                String requestedExtensionToMicroservice = extension.replace(".", "");

                if (mimeType != null && (mimeType.equals("application/pdf") || mimeType.equals("image/png") || mimeType.equals("image/jpeg"))) {
                    bytes = microserviceExport.printCustomObjectForImagesAndPDF(requestedExtensionToMicroservice, bytes);
                } else if (mimeType != null && (mimeType.equals("text/html") || mimeType.equals("text/html;charset=UTF-8"))) {
                    // if convert from html to html - do not do nothing
                } else {
                    bytes = microserviceExport.printCustomObjectForPandoc(mimeType, requestedExtensionToMicroservice, bytes);
                }

                break;

            default:
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.print.no_template_processor");
        }

        printableDataContextSaver.setBytes(bytes);
        printableDataContextSaver.setRequestedExtension(extension);
        printableDataContextSaver.setRequestedMimeType(mimeType);

        return printableDataContextSaver;
    }

    private byte[] getHandlebarsBytes(CustomObjectData customObjectData, CustomObjectTemplate customObjectTemplate, CustomObjectPrintableTemplate printableTemplate) {
        byte[] bytes;
        Template template;

        try {
            template = handlebars.compileInline(printableTemplate.getData());

            Map<String, Object> map = new HashMap<>();
            map.put("myObject", customObjectData);
            map.put("myObjectTemplate", customObjectTemplate);
            map.put("currentUser", currentUser.getCurrentUser());
            map.put("domainSettings", currentUser.getCurrentUserDomain());
            map.put("currentDate", currentUser.printWithDateFormat( new Date() ));

            Context context = Context
                    .newBuilder(map)
                    .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE)
                    .build();

            bytes = template.apply(context).getBytes(Charset.forName("UTF-8"));

        } catch (Exception e) {
            throw new InvalidRequestException(e.getMessage());
        }
        return bytes;
    }

}
