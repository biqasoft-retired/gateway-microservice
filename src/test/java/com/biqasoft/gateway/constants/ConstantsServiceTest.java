/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.constants;

import com.biqasoft.audit.object.BiqaClassService;
import com.biqasoft.entity.constants.CUSTOMER_FIELDS;
import com.biqasoft.gateway.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 11/8/2015
 * All Rights Reserved
 */

public class ConstantsServiceTest extends BaseTest {

    @Autowired
    private ConstantsService constantsService;

    @Test
    public void constantServiceTest() {
        assertTrue(constantsService.getConstantsByName(CUSTOMER_FIELDS.class.getSimpleName()).size() > 0);
        assertTrue(constantsService.getAllConstants().size() > 0);
    }

}
