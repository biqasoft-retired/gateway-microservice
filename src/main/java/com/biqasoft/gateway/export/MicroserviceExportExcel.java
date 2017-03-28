package com.biqasoft.gateway.export;

import com.biqasoft.entity.dto.export.excel.ExportCustomObjectDTO;
import com.biqasoft.entity.dto.export.excel.ExportCustomersDTO;
import com.biqasoft.entity.dto.export.excel.ExportKPIDTO;
import com.biqasoft.entity.dto.export.excel.ExportLeadGenMethodDTO;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroMapping;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.Microservice;
import org.springframework.http.HttpMethod;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/18/2016
 *         All Rights Reserved
 */
@Microservice(value = "exporter-excel", basePath = "/export/")
public interface MicroserviceExportExcel {

    @MicroMapping(path = "customer", method = HttpMethod.POST)
    byte[] getCustomersInExcel(ExportCustomersDTO customers);

    @MicroMapping(path = "kpi", method = HttpMethod.POST)
    byte[] getKPIInExcel(ExportKPIDTO exportKPIDTO);

    @MicroMapping(path = "lead_gen_method", method = HttpMethod.POST)
    byte[] getLeadGenInExcel(ExportLeadGenMethodDTO exportLeadGenMethodDTO);

    @MicroMapping(path = "custom_object", method = HttpMethod.POST)
    byte[] getCustomObjectInExcel(ExportCustomObjectDTO exportCustomObjectDTO);

}
