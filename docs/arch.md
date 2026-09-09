# AWS Payment Core
## Cloud-Native Payment Processing & Orchestration Platform

**Architecture Version:** 1.0
**Date:** September 2026
**Reference Architecture:** AWS
**Primary Stack:** Java 25, Spring Boot, Amazon EKS, Amazon MSK, Aurora PostgreSQL

---

# 1. Executive Summary

AWS Payment Core is a cloud-native payment processing and orchestration platform designed for banking and financial-services workloads.

The architecture combines:

- Synchronous processing for latency-sensitive payment authorization.
- Event-driven processing for downstream payment lifecycle activities.
- Domain-driven service boundaries.
- ISO 8583 and ISO 20022 integration.
- Double-entry financial ledger.
- Strong idempotency and transaction integrity.
- Multi-AZ high availability.
- Multi-region disaster recovery.
- PCI/security zone isolation.
- End-to-end observability and auditability.

The core design principle is:

> **Keep the financial transaction path strongly consistent and synchronous where necessary, while making the wider payment ecosystem event-driven and independently scalable.**

---

# 2. Goals

## 2.1 Functional Goals

The platform should support:

- Payment initiation.
- Payment authorization.
- Payment routing.
- Risk and fraud checks.
- Multiple payment networks.
- ISO 8583 integration.
- ISO 20022 integration.
- Payment reversal.
- Payment lifecycle tracking.
- Double-entry ledger posting.
- Clearing.
- Settlement.
- Reconciliation.
- Notifications.
- Audit and regulatory reporting.
- Merchant and customer payment data.

## 2.2 Non-Functional Goals

| Requirement | Target |
|---|---|
| Availability | 99.99%+ |
| Horizontal scalability | Yes |
| Multi-AZ | Mandatory |
| Multi-region DR | Recommended |
| Idempotency | Mandatory |
| Auditability | Mandatory |
| Encryption | At rest + in transit |
| PCI isolation | Mandatory where applicable |
| Event replay | Supported |
| Zero/low downtime deployment | Supported |
| Observability | End-to-end |

---

# 3. Architectural Principles

## 3.1 Synchronous Core, Asynchronous Ecosystem

The customer-facing payment authorization path remains synchronous:

```text
Payment Request
      |
      v
Payment Gateway
      |
      v
Payment Orchestrator
      |
      v
Risk / Routing
      |
      v
Payment Network
      |
      v
Authorization Response
      |
      v
Ledger
      |
      v
Customer Response
```

The surrounding lifecycle is event-driven:

```text
PaymentAuthorized
       |
       +--> Notification
       +--> Clearing
       +--> Settlement
       +--> Reconciliation
       +--> Analytics
       +--> Fraud monitoring
       +--> Regulatory reporting
```

This avoids introducing unnecessary asynchronous behavior into a transaction where the customer expects an immediate result.

---

# 4. High-Level Architecture

```text
                         +---------------------------+
                         |       CHANNELS            |
                         | Mobile / Web / ATM / POS |
                         | Partners / Merchants     |
                         +-------------+-------------+
                                       |
                                       v
                         +---------------------------+
                         |       AWS EDGE            |
                         | Route 53                  |
                         | CloudFront                |
                         | WAF / Shield              |
                         | API Gateway               |
                         +-------------+-------------+
                                       |
                                       v
                         +---------------------------+
                         |    PAYMENT GATEWAY        |
                         | Authentication            |
                         | Validation                 |
                         | Rate Limiting             |
                         | Idempotency               |
                         | Correlation ID             |
                         +-------------+-------------+
                                       |
                                       v
                         +---------------------------+
                         |   PAYMENT ORCHESTRATOR    |
                         | Java 25 / Spring Boot     |
                         | Amazon EKS                |
                         +-------------+-------------+
                                       |
              +------------------------+------------------------+
              |                        |                        |
              v                        v                        v
       +-------------+          +-------------+          +-------------+
       | Routing     |          | Risk        |          | Fraud       |
       | Engine      |          | Engine      |          | Detection   |
       +------+------+          +------+------+          +------+------+
              |                        |                        |
              +------------------------+------------------------+
                                       |
                                       v
                         +---------------------------+
                         |   PAYMENT ADAPTER LAYER   |
                         +-------------+-------------+
                                       |
                 +---------------------+---------------------+
                 |                     |                     |
                 v                     v                     v
          +-------------+       +-------------+       +-------------+
          | ISO 8583    |       | ISO 20022   |       | REST/API    |
          | Networks    |       | Networks    |       | Providers   |
          +------+------+       +------+------+       +------+------+
                 |                     |                     |
                 +---------------------+---------------------+
                                       |
                                       v
                              Payment Networks
                                       |
                                       v
                         +---------------------------+
                         |      LEDGER SERVICE       |
                         | Double-Entry Accounting   |
                         | Aurora PostgreSQL          |
                         +-------------+-------------+
                                       |
                               Transactional
                                  Outbox
                                       |
                                       v
                         +---------------------------+
                         |       AMAZON MSK          |
                         | Payment Event Backbone    |
                         +-------------+-------------+
                                       |
             +-------------------------+-------------------------+
             |              |             |            |         |
             v              v             v            v         v
         Clearing      Settlement   Reconciliation  Fraud   Analytics
             |              |             |            |         |
             +--------------+-------------+------------+---------+
                                       |
                                       v
                              S3 Data Lake / Archive
```

---

# 5. C4 Model

## 5.1 Level 1 — System Context

```text
                     +------------------+
                     | Customer         |
                     +--------+---------+
                              |
                              v
+-------------+      +---------------------+      +----------------+
| Merchant    |----->|                     |<-----| Banking       |
| / Partner   |      |    AWS Payment Core |      | Systems       |
+-------------+      |                     |      +----------------+
                     +----------+----------+
                                |
                    +-----------+-----------+
                    |                       |
                    v                       v
             Payment Networks        External Services
             Visa / Mastercard       Fraud / FX / KYC
             BI-FAST / SEPA          Notification
```

## 5.2 Level 2 — Container View

```text
+----------------------------------------------------------------+
|                        AWS Payment Core                        |
|                                                                |
| +------------------+        +-------------------------------+ |
| | Payment Gateway  |------->| Payment Orchestrator           | |
| +------------------+        +---------------+---------------+ |
|                                             |                 |
|                     +-----------------------+----------------+|
|                     |            |           |                |
|                     v            v           v                v
|                 Routing       Risk        Fraud          Adapter Layer
|                     |            |           |                |
|                     +------------+-----------+----------------+
|                                             |
|                                             v
|                                      Payment Networks
|                                             |
|                                             v
|                                      Ledger Service
|                                             |
|                                             v
|                                      Aurora PostgreSQL
|                                             |
|                                      Transactional Outbox
|                                             |
|                                             v
|                                      Amazon MSK
|                                             |
|                 +---------------------------+------------------+
|                 |             |              |          |       |
|                 v             v              v          v       v
|             Clearing     Settlement    Reconciliation Fraud  Analytics
|                                                                |
|                                                                v
|                                                               S3
+----------------------------------------------------------------+
```

---

# 6. Domain Model

The platform is divided into bounded contexts.

| Domain | Responsibility |
|---|---|
| Payment | Payment lifecycle |
| Orchestration | Workflow coordination |
| Routing | Selecting payment rail/provider |
| Risk | Transaction risk decisions |
| Fraud | Fraud detection |
| Ledger | Financial accounting |
| Account | Account balances |
| Clearing | Clearing files/messages |
| Settlement | Final settlement |
| Reconciliation | Internal/external matching |
| Merchant | Merchant configuration |
| Customer | Customer payment profile |
| Dispute | Chargeback/dispute lifecycle |
| Notification | Customer/merchant notifications |

---

# 7. Payment Lifecycle

```text
INITIATED
    |
    v
VALIDATED
    |
    v
RISK_CHECKED
    |
    v
ROUTED
    |
    v
SENT_TO_NETWORK
    |
    +----> TIMEOUT
    |
    v
AUTHORIZED / DECLINED
    |
    v
LEDGER_POSTED
    |
    v
CLEARED
    |
    v
SETTLED
    |
    v
RECONCILED
```

Possible exceptional states:

```text
AUTHORIZED
    |
    +--> REVERSED
    |
    +--> REFUNDED
    |
    +--> DISPUTED
```

---

# 8. Payment Orchestrator

The Payment Orchestrator is the central application coordinator.

Responsibilities:

- Validate payment commands.
- Load transaction state.
- Apply idempotency.
- Invoke risk services.
- Invoke routing engine.
- Select payment adapter.
- Manage timeout/retry policies.
- Process network responses.
- Trigger ledger posting.
- Publish lifecycle events.

It should **not** own every business rule.

Business capabilities remain in dedicated services.

---

# 9. Payment Adapter Layer

External payment networks must not leak their protocol models into the core domain.

```text
                   Canonical Payment Model
                              |
                              v
                       Adapter Interface
                              |
             +----------------+----------------+
             |                |                |
             v                v                v
        ISO8583 Adapter  ISO20022 Adapter  REST Adapter
             |                |                |
             v                v                v
        Card Network      Instant Rail     External PSP
```

The canonical model acts as an Anti-Corruption Layer.

Example:

```text
PaymentRequest
   |
   v
CanonicalPayment
   |
   +--> ISO8583Message
   |
   +--> ISO20022Message
   |
   +--> REST Provider Request
```

---

# 10. ISO 8583 Architecture

```text
Payment Core
     |
     v
ISO8583 Adapter
     |
     +--> MTI
     +--> Bitmap
     +--> Data Elements
     +--> Network Header
     |
     v
Payment Network
```

The adapter owns:

- Message packing.
- Message unpacking.
- Field mapping.
- Network management.
- Connection handling.
- Timeouts.
- Response code mapping.
- Reversal handling.

The core payment domain should remain protocol-independent.

---

# 11. ISO 20022 Architecture

For ISO 20022 rails:

```text
Payment Core
      |
      v
Canonical Payment
      |
      v
ISO20022 Adapter
      |
      v
pacs.008 / pacs.002 / pacs.004
      |
      v
Payment Rail
```

The adapter handles:

- XML generation/parsing.
- Message validation.
- Business Application Header.
- Schema validation.
- External identifiers.
- Status mapping.

---

# 12. Event-Driven Architecture

Amazon MSK is the primary event backbone.

Example topics:

```text
payment.initiated
payment.validated
payment.risk.checked
payment.routed
payment.submitted
payment.authorized
payment.declined
payment.timeout
payment.reversed
payment.failed

ledger.posted
ledger.reversed

clearing.received
settlement.completed

reconciliation.mismatch
fraud.detected
```

Kafka partitioning should be based on the consistency requirement.

Examples:

```text
payment events:
partition key = paymentId

ledger events:
partition key = accountId
```

This provides ordered processing within the required business scope.

---

# 13. Transactional Outbox

The Ledger/Payment database and Kafka must not be updated independently.

Use the Transactional Outbox pattern:

```text
                 DB Transaction
                      |
             +--------+--------+
             |                 |
             v                 v
       Payment State       Outbox Event
             |                 |
             +--------+--------+
                      |
                      v
                  Commit
                      |
                      v
              Outbox Publisher
                      |
                      v
                   MSK
```

This prevents:

```text
Database committed
Kafka publish failed
```

from leaving the system in an inconsistent state.

---

# 14. Idempotency

Payment APIs must be idempotent.

Example:

```text
POST /payments

Idempotency-Key:
ABC-123
```

First request:

```text
ABC-123 -> TX10001
```

Retry:

```text
ABC-123 -> TX10001
```

It must never create:

```text
TX10001
TX10002
```

for the same logical payment.

DynamoDB is a strong candidate for high-throughput idempotency records.

---

# 15. Financial Ledger

The ledger is a critical system of record.

Use double-entry accounting.

Example:

```text
Payment = $100

DEBIT
Customer Account       $100

CREDIT
Merchant Settlement    $100
```

Invariant:

```text
Total Debits = Total Credits
```

Ledger entries should be immutable.

Corrections should use compensating entries:

```text
Original:
DEBIT  $100

Correction:
CREDIT $100
```

rather than modifying the historical record.

---

# 16. Recommended Data Stores

| Data | AWS Service | Reason |
|---|---|---|
| Financial ledger | Aurora PostgreSQL | ACID + relational integrity |
| Payment transaction | Aurora/DynamoDB | Transactional state |
| Idempotency | DynamoDB | High-throughput key lookup |
| Cache | ElastiCache Redis | Low latency |
| Configuration | DynamoDB/Aurora | Depends on consistency |
| Events | Amazon MSK | Durable event stream |
| Archive | S3 | Durable low-cost storage |
| Search/operations | OpenSearch | Operational search |
| Analytics | S3 + Glue + Athena/Redshift | Data analytics |

---

# 17. Redis Strategy

Redis should be used as an acceleration layer, never as the financial source of truth.

Good use cases:

- Routing configuration.
- Merchant configuration.
- FX rates.
- Rate limiting.
- Short-lived cache.
- Session/token data.
- Distributed coordination where appropriate.

Avoid:

```text
Redis = authoritative account balance
```

---

# 18. AWS Compute Architecture

## Core workloads

Use Amazon EKS for:

- Payment Gateway.
- Payment Orchestrator.
- Routing Engine.
- Risk Engine.
- Fraud services.
- Ledger Service.
- Payment Adapters.
- Clearing.
- Settlement.
- Reconciliation.

Technology:

```text
Java 25
Spring Boot
Docker
Amazon EKS
```

## Event-driven supporting workloads

Use Lambda where appropriate for:

- Notifications.
- Lightweight enrichment.
- Event transformations.
- Reporting triggers.
- Non-critical background processing.

---

# 19. Messaging Strategy

Use each messaging technology for the problem it solves.

| Technology | Primary Use |
|---|---|
| Amazon MSK | High-volume durable event streams |
| SQS | Work queues and buffering |
| EventBridge | Business/integration events |
| SNS | Fan-out notifications |

Do not use one messaging technology for everything.

---

# 20. Resilience Patterns

The platform should use:

### Timeout

Every external dependency has an explicit timeout.

### Retry

Use retry only for transient failures.

```text
Retry
  |
  +--> exponential backoff
  +--> jitter
  +--> maximum attempts
```

### Circuit Breaker

```text
CLOSED
  |
  | failures
  v
OPEN
  |
  | recovery timeout
  v
HALF_OPEN
  |
  +--> CLOSED
  +--> OPEN
```

### Bulkhead

Isolate resources between:

- Payment networks.
- Merchants.
- Internal services.
- Kafka consumers.

A failing external network should not consume all platform resources.

---

# 21. Payment Timeout Handling

A timeout is not automatically a failure.

Example:

```text
Payment
   |
   v
Send ISO8583
   |
   v
Network
   |
   X
Timeout
```

The transaction may actually have been accepted by the network.

Therefore:

```text
TIMEOUT
   |
   +--> Query status
   |
   +--> Wait for response
   |
   +--> Reconciliation
   |
   +--> Reversal where required
```

This is a critical payment-system design principle.

---

# 22. Reconciliation

Reconciliation is an independent control process.

```text
Internal Transactions
        |
        v
   Reconciliation
        ^
        |
External Network File
```

Detect:

```text
Missing transaction
Duplicate transaction
Amount mismatch
Status mismatch
Settlement mismatch
Unexpected transaction
```

Results:

```text
MATCHED
MISMATCH
MISSING_INTERNAL
MISSING_EXTERNAL
AMOUNT_MISMATCH
STATUS_MISMATCH
```

Reconciliation should never rely only on real-time APIs.

---

# 23. Clearing and Settlement

Payment authorization is different from settlement.

```text
Authorization
      |
      v
Transaction
      |
      v
Clearing
      |
      v
Settlement
      |
      v
Reconciliation
```

This separation allows the architecture to model real-world payment rails accurately.

---

# 24. Security Architecture

Recommended AWS security services:

- IAM.
- AWS KMS.
- AWS Secrets Manager.
- AWS CloudHSM where HSM-backed cryptography is required.
- AWS WAF.
- AWS Shield.
- GuardDuty.
- Security Hub.
- CloudTrail.
- AWS Config.
- VPC endpoints.
- PrivateLink.

All sensitive communication should use TLS.

---

# 25. PCI Zone Isolation

Minimize the number of workloads that process sensitive card data.

```text
                NON-PCI ZONE
                     |
                     v
              Payment Platform
                     |
                Tokenization
                     |
              +------+------+
              |             |
              v             v
           Token ID     PCI Zone
                           |
                           v
                    Card-sensitive data
                           |
                           v
                         HSM
```

Prefer tokenized references outside the PCI-controlled boundary.

---

# 26. Network Architecture

```text
                         Internet
                            |
                         Route53
                            |
                       CloudFront
                            |
                           WAF
                            |
                       API Gateway
                            |
                    Transit Gateway
                            |
               +------------+------------+
               |                         |
               v                         v
          Public Subnet             Private Subnet
                                      |
                    +-----------------+----------------+
                    |                 |                |
                    v                 v                v
                   EKS               MSK             Aurora
                    |                                  |
                    +------------- Redis ---------------+
```

Core databases and Kafka should remain private.

---

# 27. Multi-AZ Architecture

Each production region should use multiple Availability Zones.

```text
                    Region
                      |
        +-------------+-------------+
        |                           |
        v                           v
      AZ-A                         AZ-B
        |                           |
      EKS                         EKS
      MSK                         MSK
      Redis                       Redis
        |                           |
        +-------------+-------------+
                      |
                    Aurora
```

No critical service should depend on a single AZ.

---

# 28. Multi-Region Strategy

Recommended model:

```text
              Global DNS / Routing
                       |
             +---------+---------+
             |                   |
             v                   v
        Region A             Region B
       Primary/Active       DR/Secondary
```

Stateless APIs and processing services can be designed for active-active operation.

Financial ledger ownership requires stronger consistency controls.

Do not assume that an active-active database automatically provides correct financial semantics.

Recommended approach:

- Active-active for stateless APIs.
- Regional ownership for financial writes.
- Controlled ledger replication.
- Automated failover procedures.
- Regular disaster-recovery testing.

---

# 29. Observability

Every transaction should carry:

```text
transactionId
paymentId
correlationId
traceId
merchantId
customerId
```

Example trace:

```text
API Gateway
    |
    v
Payment Gateway
    |
    v
Orchestrator
    |
    +--> Risk
    |
    +--> Routing
    |
    +--> Adapter
    |
    +--> Network
    |
    +--> Ledger
    |
    +--> Kafka
```

Recommended technologies:

- OpenTelemetry.
- CloudWatch.
- AWS X-Ray.
- Prometheus.
- Grafana.
- OpenSearch.

---

# 30. Operational Metrics

Important metrics include:

## Transaction

```text
TPS
Authorization latency
Success rate
Decline rate
Timeout rate
Reversal rate
```

## Network

```text
Network latency
Connection errors
Timeouts
Response-code distribution
Circuit-breaker state
```

## Kafka

```text
Consumer lag
Partition throughput
Under-replicated partitions
Producer errors
```

## Database

```text
CPU
Connection pool
Latency
Lock contention
Replication lag
```

## Financial

```text
Ledger imbalance
Reconciliation mismatch
Settlement variance
Duplicate transaction count
```

---

# 31. Disaster Recovery

Recovery strategy should distinguish:

### Stateless services

Can be redeployed quickly.

### Transaction state

Must be recoverable.

### Ledger

Requires the strongest recovery guarantees.

### Kafka

Requires durable replicated event storage.

### Historical/audit data

Stored in S3 with appropriate retention and immutability controls.

DR testing should include:

```text
AZ failure
EKS node failure
Kafka broker failure
Database failure
Network failure
Payment network outage
Region failure
```

---

# 32. Deployment Strategy

Use:

```text
Git
 |
 v
CI
 |
 +--> Unit tests
 +--> Integration tests
 +--> Contract tests
 +--> Security scanning
 +--> Container scanning
 |
 v
Container Registry
 |
 v
EKS
 |
 +--> Canary
 +--> Blue/Green
 +--> Rolling deployment
```

For payment services, canary deployment is preferred where practical.

---

# 33. Testing Strategy

## Unit Testing

Business rules and domain logic.

## Integration Testing

- Aurora.
- MSK.
- Redis.
- External adapters.

## Contract Testing

Verify compatibility between:

```text
Payment Core <-> Adapter
Adapter <-> Network
Producer <-> Consumer
```

## Performance Testing

Test:

```text
Normal TPS
Peak TPS
Burst TPS
Sustained load
Network latency
Kafka backlog
Database contention
```

## Failure Testing

Simulate:

```text
Network timeout
Kafka outage
Database failover
Duplicate request
Delayed response
Partial failure
Consumer crash
```

---

# 34. Example End-to-End Card Payment

```text
1. Merchant
      |
2. API Gateway
      |
3. Payment Gateway
      |
4. Idempotency check
      |
5. Payment Orchestrator
      |
6. Risk Engine
      |
7. Routing Engine
      |
8. ISO8583 Adapter
      |
9. Card Network
      |
10. Authorization response
      |
11. Ledger Service
      |
12. Transactional Outbox
      |
13. Amazon MSK
      |
14. Notification
      |
15. Clearing
      |
16. Settlement
      |
17. Reconciliation
```

---

# 35. Example Instant Payment

```text
Customer
   |
   v
Payment API
   |
   v
Payment Orchestrator
   |
   v
Risk
   |
   v
ISO20022 Adapter
   |
   v
Instant Payment Rail
   |
   v
pacs.002
   |
   v
Ledger
   |
   v
PaymentAuthorized
   |
   v
MSK
   |
   +--> Notification
   +--> Analytics
   +--> Reconciliation
```

---

# 36. Technology Decision Matrix

| Capability | Recommended AWS Technology |
|---|---|
| API | API Gateway |
| Edge | CloudFront |
| WAF | AWS WAF |
| Compute | EKS |
| Container registry | ECR |
| Event streaming | MSK |
| Queue | SQS |
| Business events | EventBridge |
| Database | Aurora PostgreSQL |
| Key/value state | DynamoDB |
| Cache | ElastiCache Redis |
| Object storage | S3 |
| Search | OpenSearch |
| Analytics | Athena / Redshift |
| Encryption | KMS |
| Secrets | Secrets Manager |
| HSM | CloudHSM |
| Monitoring | CloudWatch |
| Tracing | OpenTelemetry / X-Ray |
| Security | GuardDuty / Security Hub |
| DNS | Route 53 |

---

# 37. Key Architecture Decisions

## ADR-001 — EKS for Core Payment Services

**Decision:** Use Amazon EKS.

**Reason:**

- Predictable latency.
- Long-running JVM services.
- Kubernetes expertise.
- Horizontal scaling.
- Strong control over runtime.
- Suitable for high-volume transaction processing.

---

## ADR-002 — MSK as Event Backbone

**Decision:** Use Amazon MSK for durable payment events.

**Reason:**

- High throughput.
- Partitioning.
- Ordering.
- Replay.
- Durable event history.
- Strong fit for event-driven payment processing.

---

## ADR-003 — Aurora PostgreSQL for Ledger

**Decision:** Use Aurora PostgreSQL for the authoritative ledger.

**Reason:**

- ACID transactions.
- Relational integrity.
- Strong consistency.
- Mature SQL ecosystem.
- Suitable for double-entry accounting.

---

## ADR-004 — Transactional Outbox

**Decision:** Use Transactional Outbox for database-to-Kafka publishing.

**Reason:**

Prevents inconsistent states between database commits and event publication.

---

## ADR-005 — Canonical Payment Model

**Decision:** Keep ISO 8583/ISO 20022 models outside the core payment domain.

**Reason:**

- Reduces protocol coupling.
- Simplifies adding new payment networks.
- Enables protocol-independent business logic.
- Creates a clean Anti-Corruption Layer.

---

# 38. Main Risks

| Risk | Mitigation |
|---|---|
| Duplicate payment | Idempotency |
| Network timeout | Status inquiry + reconciliation |
| Kafka failure | Replication + retry |
| Database failure | Multi-AZ + backups |
| Provider outage | Circuit breaker |
| Consumer failure | Kafka replay |
| Ledger corruption | Immutable journal + reconciliation |
| Region outage | DR region |
| PCI scope expansion | Tokenization + isolation |
| Event inconsistency | Transactional Outbox |
| Double spending | Strong ledger consistency |

---

# 39. Architecture Quality Attributes

## Availability

Multi-AZ EKS, MSK and Aurora.

## Performance

Low-latency synchronous path with horizontally scalable services.

## Scalability

Kafka partitioning and EKS horizontal scaling.

## Reliability

Retries, circuit breakers, bulkheads and reconciliation.

## Security

Defense-in-depth plus PCI isolation.

## Maintainability

DDD, bounded contexts and protocol adapters.

## Observability

Distributed tracing and transaction correlation.

## Auditability

Immutable events, ledger entries and S3 archives.

---

# 40. Recommended Showcase Implementation

For an architecture portfolio, implement the following three flows first.

## Showcase #1 — Card Payment

```text
REST
 |
Payment Gateway
 |
Orchestrator
 |
Risk
 |
Routing
 |
ISO8583 Adapter
 |
Mock Card Network
 |
Ledger
 |
MSK
```

## Showcase #2 — Instant Payment

```text
REST
 |
Payment Orchestrator
 |
Risk
 |
ISO20022 Adapter
 |
Mock Instant Payment Rail
 |
Ledger
 |
MSK
```

## Showcase #3 — Payment Lifecycle

Demonstrate:

```text
Authorization
      |
      v
Ledger
      |
      v
Kafka
      |
 +----+----+---------+
 |         |         |
 v         v         v
Clearing Settlement Reconciliation
```

---

# 41. Suggested Repository Structure

```text
aws-payment-core/
│
├── README.md
├── ARCHITECTURE.md
├── arc42/
│   ├── 01-introduction.md
│   ├── 02-constraints.md
│   ├── 03-context.md
│   ├── 04-solution-strategy.md
│   ├── 05-building-blocks.md
│   ├── 06-runtime-view.md
│   ├── 07-deployment-view.md
│   ├── 08-concepts.md
│   ├── 09-decisions.md
│   ├── 10-quality.md
│   └── 11-risks.md
│
├── docs/
│   ├── c4/
│   ├── sequence/
│   ├── adr/
│   └── aws/
│
├── services/
│   ├── payment-gateway/
│   ├── payment-orchestrator/
│   ├── routing-engine/
│   ├── risk-engine/
│   ├── ledger-service/
│   ├── iso8583-adapter/
│   ├── iso20022-adapter/
│   ├── clearing-service/
│   ├── settlement-service/
│   └── reconciliation-service/
│
├── infrastructure/
│   ├── terraform/
│   └── kubernetes/
│
└── tests/
    ├── integration/
    ├── contract/
    └── performance/
```

---

# 42. Final Architecture Position

The recommended architecture can be summarized as:

```text
                 AWS PAYMENT CORE

          SYNCHRONOUS TRANSACTION CORE
                     |
        +------------+------------+
        |            |            |
      Risk        Routing      Adapter
        |            |            |
        +------------+------------+
                     |
              Payment Network
                     |
                  Ledger
                     |
              Transactional
                 Outbox
                     |
                     v
              EVENT-DRIVEN CORE
                     |
                    MSK
                     |
       +-------------+-------------+
       |             |             |
   Clearing      Settlement   Reconciliation
       |
   Analytics / Fraud / Notification
```

The architectural philosophy is:

> **Strong consistency for money.**
>
> **Event-driven architecture for scale and decoupling.**
>
> **Protocol isolation for payment networks.**
>
> **Immutable accounting for financial integrity.**
>
> **AWS managed services where they provide operational advantage.**
>
> **EKS for the core Java payment runtime.**

This architecture is intentionally designed as a **bank-grade payment platform**, not merely a collection of microservices.
