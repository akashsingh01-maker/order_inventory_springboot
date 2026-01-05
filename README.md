# order_inventory_springboot

## Architecture overview
- Microservices: API Gateway, Eureka (service discovery), Order Service, Inventory Service, Zipkin (tracing), Postgres (inventory DB).
- Clean separation: controllers (api), application/use-cases (service layer), persistence (JPA entities/repositories), integration (HTTP clients). Order and Inventory are separate services communicating over HTTP.
- Observability: Spring Cloud Sleuth + Zipkin for distributed tracing and Micrometer for metrics (Prometheus friendly).

## Design decisions
- Java 17 compatibility for current environment; Maven multi-module-like layout with each service owning its own pom and Dockerfile.
- Idempotency: Idempotency-Key supported for POST operations. Example implementation uses an in-memory store (demo); production should use Redis or a DB table with a unique constraint.
- Error model: consistent JSON error structure ({ code, message, details, requestId, timestamp }). Controllers add requestId via `X-Request-ID` header and MDC for structured logs.
- One-command run: docker-compose builds and starts all services (Eureka, API Gateway, Order, Inventory, Postgres, Zipkin). Use `docker-compose up --build`.

## Concurrency strategy
- Prevent oversell using pessimistic row-level locking in Inventory service:
  - Inventory repository exposes a `findByIdForUpdate` method using `PESSIMISTIC_WRITE`.
  - Reservation performed in Inventory service within a DB transaction: lock product rows (in deterministic order), check availability, decrement `available` and increment `reserved`, then commit.
  - Order confirmation flow calls Inventory reservation endpoints and only marks order CONFIRMED after reservation succeeds.
- Deadlock avoidance: always lock product rows in sorted order (by product id). Keep transactions short and set sensible lock timeouts + retry policy in production.

## Security approach
- API Gateway enforces Bearer JWT. Demo gateway includes a minimal JWT check (uses `Authorization: Bearer testtoken` for local demo). Replace with proper JWT signature verification in production.
- AuthZ: validate token scopes/claims (e.g., sub for customer id or service roles). Return 401 for unauthenticated, 403 for unauthorized.
- Transport: run services behind TLS in production.

## How to run locally
1. Prerequisites
   - Docker and docker-compose installed
   - (Optional) Java to run services locally without containers

2. Start everything (single command)
   - PowerShell:
     docker-compose up --build
   - To run detached:
     docker-compose up --build -d

3. Useful endpoints
   - API Gateway: http://localhost:8080
   - Eureka: http://localhost:8761
   - Zipkin: http://localhost:9411
   - Postgres: localhost:5432 (DB: inventorydb / user: postgres / password: postgres)

4. Demo usage notes
   - API Gateway demo security expects header: `Authorization: Bearer testtoken`.
   - Use `Idempotency-Key` header for POST /orders and POST /orders/{id}/confirm/cancel to ensure idempotent behavior.
   - Use `X-Request-ID` to propagate a request correlation id; services will generate one if missing.

## Tests
- Unit tests, Testcontainers-based integration scaffolding and a concurrency test are present under `order-service/src/test` and `inventory-service/src/test`. Run tests with Maven in each module:
  mvn -pl order-service test
  mvn -pl inventory-service test

## Next steps / Production hardening
- Replace in-memory idempotency with Redis/DB-backed store and add unique constraint.
- Harden JWT validation (public key, claim checks) and add RBAC if needed.
- Add Flyway or Liquibase DB migrations.
- Configure Prometheus scraping and Grafana dashboards for metrics.
- Add retries/backoff for inter-service calls and circuit breakers for resilience.