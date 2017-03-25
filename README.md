# Public REST API gateway

 - [API gateway pattern - Microservices architecture](http://microservices.io/patterns/apigateway.html)

# [e2e tests with postman](src/test/e2e)

 - behind this microservice must be proxy(e.g nginx), which deny access for `/internal/**` from internet.
 
## Properties

| Option                                           | example                                           | mandatory | description                                                                                                                                                                                             |
| ------------------------------------------------ | ------------------------------------------------- | --------- | ------------------------------------------------------------------------------------------------- |
| biqa.urls.http.async                             |   http://localhost:9096                           |    yes    | async gateway url |
