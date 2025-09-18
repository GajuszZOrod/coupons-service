# Deployment
This is a Java SpringBoot application that manages coupons creation and usage. For the app to work, it must have network access to following components:
- MySQL database (tested with MySQL 9.2)
- Redis cache server (tested with Redis 8.2.1)
- https://ipwho.is webpage

The database is used to store coupons information.
The ipwho.is is an external service for checking users' IP address.
The cache server allows for reducing number of requests to both the database and the external service.

The application is designed to run in potentially multiple instances that will share a common database and a common cache server.

#### Sharding
For scalability and optimization purposes, it is also possible to deploy it in the sharding pattern. For example, you can define separate shards for different world regions, e.g. Central Europe shard and North America shard. Each shard runs its own database and cache server. Instances of the applications that are running within a shard will be configured to only support coupons and users of particular countries. Sharding deployment requires a proper routing, in order to connect users to the appropriate shard, that is, to appropriate group of instances of the application. Running the application in shard mode is optional and disabled in the default configuration.

# Execution

### 🟢 Quickest way to run locally
1. Clone the repository
2. Use bash / Git Bash commandline, enter the cloned folder
3. Make sure you have docker / Docker Desktop installed and active in your system
4. Run command: `docker compose up --build`
>Note: it starts three containers that use host ports: 7400, 3399, 6399. In case of issues, make sure you have these ports free, or change them to different values in the docker-compose.yml file.
5. Application ready. Try sending POST requests:
  * `http://localhost:7400/v1/coupons` with JSON body `{ "code": "Lato", "usagesMax": 10, "country": "PL" }`
  * `http://localhost:7400/v1/coupons-usages` with JSON body `{ "code": "Lato", "userId": 111, "userIpAddress": "194.181.92.102" }`
>Note: The application connects https://ipwho.is geolocalization service. In case of issues, make sure this address is available from your environment.

### Development
If you use maven to build or test the app, note that the integration tests run docker containers (redis, mysql, wiremock), so you must have a docker or Docker Desktop installed and ready in your system.

### Parameters
Here is the list of environmental variables that you can set to configure application behavior. You likely need to set most of them, depending on your deployment:
  
|parameter| default value                      |
| ------------- |------------------------------------|
|COUPONS_PORT| 7400                               |
|COUPONS_SUPPORTED_COUNTRIES|(empty list)|
|COUPONS_DB_URL|jdbc:mysql://localhost:3306/coupon|
|COUPONS_DB_USER|coupon|
|COUPONS_DB_PASS|SecretPass2.|
|COUPONS_REDIS_HOST|localhost|
|COUPONS_REDIS_PORT|6379|
- To populate database with the required tables, use the `database.sql` file from repository.
- If you intend to run in shard mode, here is example value of the related parameter: `COUPONS_SUPPORTED_COUNTRIES=PL,CZ,DE`