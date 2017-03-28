package com.biqasoft.gateway.export;

import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroMapping;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroPathVar;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.Microservice;
import org.springframework.http.HttpMethod;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/18/2016
 *         All Rights Reserved
 */
@Microservice(value = "exporter", basePath = "/export/v1/")
public interface MicroserviceExport {

    @MicroMapping(path = "from/html/to/{toFormat}", method = HttpMethod.POST)
    byte[] printCustomObjectForImagesAndPDF(@MicroPathVar("toFormat") String toFormat, byte[] payload);

    @MicroMapping(path = "from/html/to/pandoc?mime_type={mimeType}&extension={extension}", method = HttpMethod.POST)
    byte[] printCustomObjectForPandoc(@MicroPathVar("mimeType") String mimeType,
                                      @MicroPathVar("extension") String extension, byte[] payload);

}
