# Campaign finance API
![Build](https://github.com/rockamorales/campaign-finance-api/actions/workflows/scala.yml/badge.svg)

This API provides the functionality required to keep track of campaign contributions, is able to register
individual contributions and contributions thru PACs (political action committees)

It provides endpoints that allow to register committees, individuals, candidates, and individual 
and committees contributions

## Scope
### Security: 
Basic level of security based on JWT tokens. As part of the security administration and authentication mechanisms, following endpoiunts will be provided:
   * /api/v1_0/login -> requires username & password, a token with an expiration date will be provided
In order to simplify the security, which is usually handled by some API Gateway and not by the service itself, I will not provide and sign up or any other means to create users and roles
### Error handling: 
not completely understood yet. For now, each endpoint will define a set of HTTP codes along with some ErrorInfo object that should be returned as part of the response body. *TODO:* Not sure yet, if I should implement a generic error handler

### Endpoints:
   Check swagger documentation in: ...

### Application code structure
The application consists of the following structure. At the base package
1. Base package: com.smartsoft contains three classes that are application specific: 
  - APIModule contains MacWire DI declarations
  - CampaignFinanceAPIApp: application entry point. 
  - CampaignFinanceAPIEndpoints: Generate docs & metrics endpoints and convert endpoints to akka http routes
2. services: com.smartsoft.services contains all the services classes, which provide the logic to process data thru actors
3. security: com.smartsoft.security contains all the security related classes, Authentication logic, JWT token generation & validation logic
4. server: com.smartsoft.server contains all classes related to the specific server implementation. For this project akka-http server initialization logic
5. model: com.smartsoft.model contains all model related classes
6. controllers: each controller class provides all endpoints related to a specific entity
7. actors: All actor classes and any other related class/object

### Metrics and Monitoring: 
Use prometheus to collect metrics and Graphana to present the data. More details coming up soon...

### Akka actors serialization: 
exploring options, for now, default java serialization will be used

### Application high level architecture

![](docs/API_Architecture.png "Application Architecture")

### Use of HTTPS protocol: 
to be defined ...


