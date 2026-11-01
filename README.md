# Order Inventory Platform

Microservices demo built for a full-stack Java interview: Spring Boot services with
synchronous (REST/Feign) and asynchronous (Kafka) communication, Spring Security +
JWT authN/authZ, SQL Server + MongoDB (database-per-service), React + Redux Toolkit +
Axios frontend, Docker, Jenkins, deployable to AWS.

## Modules

| Module | Port | DB | Purpose |
|---|---|---|---|
| eureka-server | 8761 | - | Service registry |
| api-gateway | 8080 | - | Single entry point, routes to services, edge JWT check |
| auth-service | 8081 | SQL Server (authdb) | Register/login, issues JWT, roles |
| order-service | 8082 | SQL Server (orderdb) | Places orders; sync call to inventory; publishes Kafka event |
| inventory-service | 8083 | MongoDB (inventorydb) | Product catalog/stock; consumes Kafka event |
| notification-service | 8084 | MongoDB (notificationdb) | Consumes Kafka event, logs a "sent" notification |
| frontend | 3000 | - | React UI |
| common-security | - | - | Shared JwtUtil + JwtAuthFilter jar used by every servlet-based service |

## How a request flows

1. **Login (sync)**: React -> Gateway -> auth-service validates credentials (BCrypt), returns a JWT with `roles` claim.
2. **Place order (sync + async)**:
   - React attaches `Authorization: Bearer <jwt>` (Axios interceptor) -> Gateway -> order-service.
   - **Sync**: order-service calls inventory-service via Feign (`InventoryClient`, load-balanced through Eureka, wrapped in a Resilience4j circuit breaker) to check stock, forwarding the same JWT.
   - Order is saved in SQL Server, response returned immediately.
   - **Async**: order-service publishes `OrderPlacedEvent` to Kafka topic `order-events`. inventory-service (consumer group `inventory-service`) decrements real stock; notification-service (consumer group `notification-service`) logs a confirmation. Order-service never waits on either.
3. **Authorization**: every service independently validates the JWT (`JwtAuthFilter` from `common-security`) and enforces roles with `@PreAuthorize("hasRole('ADMIN')")` etc. The Gateway also rejects missing/invalid tokens at the edge before routing (defense in depth).

## Running it locally (fastest path while you're learning/debugging)

### 1. Start infrastructure only

```bash
docker-compose up -d
```

This starts SQL Server, MongoDB, Zookeeper, Kafka. Give SQL Server ~30s to finish initializing before starting auth-service/order-service.

### 2. Create the two SQL Server databases

Connect with any SQL client (Azure Data Studio, SSMS, or `sqlcmd`) to `localhost,1433` (user `sa`, password `YourStrong!Passw0rd`) and run:

```sql
CREATE DATABASE authdb;
CREATE DATABASE orderdb;
```

(MongoDB databases `inventorydb` / `notificationdb` are created automatically on first write.)

### 3. Build everything

```bash
mvn clean install
```

### 4. Start each service (separate terminals, in this order)

```bash
cd eureka-server && mvn spring-boot:run
```
Wait for it to come up (http://localhost:8761), then in new terminals:

```bash
cd auth-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

Check http://localhost:8761 — all 5 services should show as `UP`.

### 5. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:3000.

## Testing the API directly (before touching the UI)

```bash
# Register an admin and a customer
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"username":"admin1","password":"Pass123!","email":"admin1@test.com","role":"ADMIN"}'

curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"username":"cust1","password":"Pass123!","email":"cust1@test.com","role":"CUSTOMER"}'

# Login as admin, grab the token
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin1","password":"Pass123!"}'

# Create a product (replace TOKEN)
curl -X POST http://localhost:8080/api/inventory -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"name":"Laptop","description":"14-inch","price":1200,"stockQuantity":10}'

# Login as customer, place an order (replace TOKEN and productId)
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"items":[{"productId":"<id>","productName":"Laptop","quantity":2,"price":1200}]}'
```

Watch the order-service/inventory-service/notification-service console logs — you'll
see the synchronous stock check happen first, the order get saved, then the Kafka
event fan out to the other two services asynchronously.

## What's documented but not live-deployed (time-boxed for interview prep)

- `Jenkinsfile` — real pipeline (build, test, Docker build, push, deploy stage) but needs an actual Jenkins instance + registry credentials to run.
- `aws/` — AWS service mapping (RDS, DocumentDB, MSK, ECR, ECS/Fargate, Secrets Manager) and a sample ECS task definition. Explaining this mapping in the interview is usually worth more than a live deployment.
- Per-service `Dockerfile` — works once you `mvn package` each module (`COPY target/*.jar app.jar`); not wired into docker-compose by default so you get fast local iteration while developing.

## Talking points for the interview

- Database-per-service: SQL Server for relational data (orders/users with FK-ish structure), MongoDB for flexible catalog/log data — justify the choice per service, not just "because I was told to."
- Why sync *and* async between Order and Inventory: sync gives immediate user feedback (can we even fulfil this?); async decouples the actual stock mutation + notification so Order isn't blocked by Inventory/Notification being slow or down.
- Circuit breaker on the sync call: what happens to the order flow if inventory-service is down (fails fast via fallback instead of hanging).
- Stateless JWT auth: no shared session store; every service independently verifies the same signed token; gateway does a coarse check, each service does fine-grained `@PreAuthorize` role checks.
- Token propagation: Feign `RequestInterceptor` forwards the caller's JWT on the service-to-service call.
