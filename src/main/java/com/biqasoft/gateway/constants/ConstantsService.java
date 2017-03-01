/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.constants;

import com.biqasoft.audit.object.BiqaClassService;
import com.biqasoft.entity.constants.*;
import com.biqasoft.entity.core.BaseClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * show field from object (which is constants, such as roles, data sources types etc)
 */
@Service
public class ConstantsService {

    private Map<String, List<String>> allConstants = new HashMap<>();

    @Autowired
    public ConstantsService(BiqaClassService biqaClassService) {
        List<Class> classes = new ArrayList<>();

        classes.add(CUSTOM_FIELD_TYPES.class);
        classes.add(CUSTOMER_FIELDS.class);
        classes.add(DATA_SOURCES.class);
        classes.add(DATA_SOURCES_HISTORY_TYPES.class);
        classes.add(DATA_SOURCES_RETURNED_TYPES.class);
        classes.add(DATE_CONSTS.class);
        classes.add(DOCUMENT_FILE.class);
        classes.add(SALES_FUNNEL.class);
        classes.add(SYSTEM_FIELDS_CONST.class);
        classes.add(SYSTEM_ROLES.class);
        classes.add(TOKEN_TYPES.class);
        classes.add(WIDGET_CONTROLLERS.class);
        classes.add(WIDGET_LOCATION.class);

        /**
         * make classes that extends {@link BaseClass}
         * visible via `GET http://localhost:8080/constants/name/MAIN_OBJECTS`
         */
        classes.forEach(x -> allConstants.put(x.getSimpleName(), getListOfStringsFromFields(x)));

        List<String> biqaObjects = new ArrayList<>();
        biqaClassService.getBiqaClasses().forEach( (x, y) -> biqaObjects.add(x) );
        allConstants.put("MAIN_OBJECTS", biqaObjects);
    }

    private List<String> getListOfStringsFromFields(Class classs) {
        List<String> res = new ArrayList<>();
        Arrays.asList(classs.getFields()).forEach(x -> {
            try {
                x.setAccessible(true);
                Object o = x.get(classs);
                if (o instanceof String) {
                    res.add((String) o);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
        return res;
    }

    public List<String> getConstantsByName(String name) {
        return allConstants.get(name);
    }

    public Map<String, List<String>> getAllConstants() {
        return allConstants;
    }

    public void addConstant(String key, List<String> value) {
        allConstants.put(key, value);
    }

}
