/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateServiceRequestContext {

    /**
     * get diff between dates in MILLISECONDS
     *
     * @param date1
     * @param date2
     * @param timeUnit TimeUnit.MILLISECONDS
     * @return
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, timeUnit);
    }

}
