# arc42

## 1. Introduction and Goals

### 1.1 Purpose
This document defines the architecture of AWS Payment Core, a cloud-native payment processing and orchestration platform for financial institutions and payment partners. It captures the system context, major building blocks, runtime behavior, deployment model, and key quality goals.

### 1.2 Scope
The platform supports:
- payment initiation, authorization, and routing
- risk and fraud evaluation
- integration with ISO 8583, ISO 20022, and REST-based providers
- ledger posting and double-entry accounting
- downstream lifecycle processing such as clearing, settlement, reconciliation, notifications, and analytics

### 1.3 Goals
Primary goals:
- process customer-facing payment authorization synchronously with predictable latency
- keep the financial transaction path consistent and auditable
- use event-driven processing for downstream lifecycle operations
- support high availability, strong idempotency, and replayability
- maintain PCI-sensitive boundaries and regulatory traceability

### 1.4 Non-Functional Requirements
- Availability: 99.99%+
- Multi-AZ deployment
- Regional disaster recovery capability
- Strong idempotency for payment requests and downstream effects
- Encryption in transit and at rest
- End-to-end observability and correlation IDs
- Auditability of state changes and ledger events
- Zero/low-downtime deployability

---

## 2. Architecture Constraints

- Java 25 and Spring Boot are used for core services and orchestration logic.
- AWS-native deployment is the target platform: Amazon EKS, Aurora PostgreSQL, and Amazon MSK.
- Payment flows must remain financially consistent and auditable.
- The platform must support multiple payment standards: ISO 8583, ISO 20022, and REST APIs.
- Strong isolation is required for PCI-relevant processing and regulated data handling.
- The system must support replay of events and recoverability of processing state.

---

## 3. Context and Scope

### 3.1 Business Context
The platform interacts with customers, merchants, banking systems, payment networks, and external services such as fraud providers, communication gateways, and regulatory reporting tools.

```text
Customer / Merchant / Partner
        |
        v
Payment Channels
        |
        v
AWS Payment Core
        |--------------------------------------|
        |                                      |
        v                                      v
Payment Networks                     External Services
Visa / Mastercard / SEPA / BI-FAST    Fraud / FX / KYC / Notifications
```

### 3.2 System Context
The payment system accepts payment requests, validates them, performs risk checks, routes them to appropriate networks, records financial state in a ledger, and emits events for downstream processing.

```text
+------------------+      +--------------------------+
| Customer /       | ---> | AWS Payment Core         |
| Merchant /       |      | - Gateway                |
| Partner          |      | - Orchestrator           |
+------------------+      | - Risk / Fraud / Routing |
                           | - Ledger / Eventing      |
                           +-----------+--------------+
                                       |
                  +--------------------+--------------------+
                  |                                         |
                  v                                         v
      Payment Networks                         External Services
      Visa / Mastercard / SEPA                 Fraud / FX / Notification
```

---

## 4. Solution Strategy

### 4.1 Core Design Pattern
The system follows a hybrid architecture:
- synchronous processing for customer-facing authorization and final confirmation
- asynchronous event-driven processing for all non-critical lifecycle activities

This keeps the transaction path responsive while enabling independent scale-out for downstream services.

### 4.2 Domain decomposition
The application is organized around payment-domain responsibilities:
- payment gateway and API entry points
- orchestration and orchestration state management
- risk evaluation and fraud detection
- routing and provider selection
- adapter layer for network protocols
- ledger and accounting services
- messaging and event distribution

### 4.3 Technical Strategy
- Spring Boot services hosted on Amazon EKS
- PostgreSQL in Aurora for transactional ledger and financial state
- Kafka-based event backbone via Amazon MSK for asynchronous processing
- Outbox pattern for reliable event publication
- standardized correlation IDs and idempotency keys
- platform-level observability using logs, metrics, traces, and audits

---

## 5. Building Block View

### 5.1 Level 1: Major Components

```text
+---------------------------+
| Payment Gateway            |
| - auth                     |
| - validation               |
| - rate limiting            |
| - idempotency              |
| - correlation tracking     |
+-------------+-------------+
              |
              v
+---------------------------+
| Payment Orchestrator       |
| - payment workflow        |
| - state machine           |
| - routing                 |
| - risk / fraud orchestration |
+-------------+-------------+
              |
      +-------+---------+--------+
      |                 |        |
      v                 v        v
+----------------+  +----------------+  +----------------+
| Routing Engine|  | Risk Engine    |  | Fraud Engine   |
+----------------+  +----------------+  +----------------+
      |                 |        |
      +-----------------+--------+
                        |
                        v
+---------------------------+
| Adapter Layer             |
| - ISO 8583                |
| - ISO 20022              |
| - REST / provider APIs    |
+-------------+-------------+
              |
              v
+---------------------------+
| Payment Networks          |
+---------------------------+
              |
              v
+---------------------------+
| Ledger Service            |
| - double-entry            |
| - transaction record      |
| - posting and settlement  |
+-------------+-------------+
              |
              v
+---------------------------+
| Event Backbone (MSK)      |
| - PaymentAuthorized       |
| - PaymentSettled          |
| - NotificationRequest     |
| - ReconciliationEvent     |
+-------------+-------------+
              |
              v
+---------------------------+
| Downstream Consumers      |
| - Clearing                |
| - Settlement              |
| - Reconciliation          |
| - Notification            |
| - Analytics / Reporting   |
+---------------------------+
```

### 5.2 Key Responsibilities
- Gateway: ingress validation, customer authentication, idempotency, rate limiting, correlation IDs.
- Orchestrator: payment event flow coordination and transactional state progression.
- Risk / Fraud: policy checks and decision-making.
- Routing: partner/network selection and endpoint resolution.
- Ledger: authoritative financial state with accounting integrity.
- Event Backbone: decouples synchronous processing from business lifecycle consumers.

---

## 6. Runtime View

### 6.1 Payment Authorization Flow
```text
Payment Request
      |
      v
Gateway
      |
      v
Validate ID / Auth / Rate / Correlation
      |
      v
Orchestrator
      |
      +--> Risk Check
      |
      +--> Routing
      |
      +--> Network Adapter
      |
      v
Payment Network Response
      |
      v
Ledger Posting
      |
      v
Emit PaymentAuthorized event
      |
      v
Return response to caller
```

### 6.2 Event-Driven Follow-On Processing
```text
PaymentAuthorized
   |
   +--> Notification Service
   +--> Clearing Service
   +--> Settlement Service
   +--> Reconciliation Service
   +--> Analytics / Monitoring
   +--> Fraud Monitoring
   +--> Regulatory Reporting
```

### 6.3 Runtime Principles
- Every payment request has a unique correlation identifier and idempotency key.
- Ledger posting is authoritative and transactional.
- Event publishing occurs only after the core transaction state is durable.
- Consumers are independently scalable and can replay events when needed.

---

## 7. Deployment View

### 7.1 Target Deployment Topology
The system is designed for AWS cloud-native deployment with resilience across multiple availability zones.

```text
+-----------------------------------------------------------+
| AWS Region A                                              |
|                                                           |
| +------------------+      +------------------------------+ |
| | Route 53 / WAF   | ---> | API Gateway / Edge Layer     | |
| +------------------+      +---------------+--------------+ |
|                                             |                |
|                                             v                |
|                                   +----------------------+ |
|                                   | EKS Cluster          | |
|                                   | - Gateway            | |
|                                   | - Orchestrator        | |
|                                   | - Risk / Routing     | |
|                                   | - Adapters           | |
|                                   +----------+-----------+ |
|                                              |               |
|                                              v               |
|                                   +----------------------+ |
|                                   | Aurora PostgreSQL    | |
|                                   | Ledger / metadata    | |
|                                   +----------------------+ |
|                                              |               |
|                                              v               |
|                                   +----------------------+ |
|                                   | Amazon MSK           | |
|                                   | Event backbone       | |
|                                   +----------------------+ |
|                                              |               |
|                                  +-----------+-----------+ |
|                                  | Downstream services   | |
|                                  | clearing / settlement | |
|                                  | notification / audit  | |
|                                  +-----------------------+ |
+-----------------------------------------------------------+
```

### 7.2 Deployment Characteristics
- horizontal scaling through EKS-managed workloads
- stateless processing components behind load balancers
- persistent transactional data in Aurora PostgreSQL
- Kafka topic-based event distribution
- multi-AZ replication for resilience
- infrastructure suitable for multi-region DR patterns

---

## 8. Cross-Cutting Concepts

### 8.1 Idempotency and Reconciliation
Payment systems must not double-post or replay side effects unexpectedly. The architecture therefore requires:
- client-supplied idempotency keys or server-generated identifiers
- stored request fingerprints
- deduplication logic on gateway and orchestration boundaries
- ledger reconciliation with event stream verification

### 8.2 Security and Compliance
- encryption in transit and at rest
- separation of PCI-relevant systems from general workloads
- private networking where possible
- fine-grained IAM and service-to-service trust boundaries
- audit logs for all state transitions and financial events

### 8.3 Observability
The platform should provide:
- request correlation across APIs and events
- metrics for latency, queue depth, processing errors, and retry counts
- structured logs with payment identifiers and correlation IDs
- alerts for failure states in authorization, ledger, and downstream processing

### 8.4 Data Integrity
The architecture treats the ledger as the source of truth. Business events are generated based on committed state, not on optimistic assumptions. This prevents drift between internal processing state and accounting records.

---

## 9. Architecture Decisions

### 9.1 Synchronous core, asynchronous ecosystem
Customer-facing authorization remains synchronous to satisfy latency expectations. All adjacent operational processing is decoupled from the critical path using events.

### 9.2 Double-entry ledger
A ledger-based accounting model is required to preserve financial integrity and provide a reliable foundation for reconciliation and audit reporting.

### 9.3 Event-driven downstream processing
Notification, clearing, settlement, and analytics are separated from the transaction path to allow independent scaling and operational resilience.

### 9.4 Protocol abstraction via adapters
Different payment networks expose different protocols and behaviors. A dedicated adapter layer isolates those variations from the orchestrator.

### 9.5 Outbox-based event publication
To prevent event loss between transaction commit and event dispatch, the architecture uses transactional outbox patterns or equivalent durable publication guarantees.

---

## 10. Quality Requirements

### 10.1 Functional Quality
- correct payment authorization and routing
- accurate accounting
- support for cancellations/reversals
- lifecycle visibility across all downstream activities

### 10.2 Performance Quality
- fast synchronous authorization responses
- predictable latency under load
- capacity for spikes in payment traffic

### 10.3 Reliability Quality
- resilient to network and service failures
- retry-safe event processing
- durable ledger and event storage
- recovery from partial failures without state corruption

### 10.4 Security Quality
- protected access to payment and customer data
- restricted network paths and least-privilege access
- data minimization and segmentation where regulated workloads apply

---

## 11. Risks and Technical Debts

- Payment systems require careful protocol and data-contract discipline; small schema mismatches can cause costly reconciliation issues.
- Event-driven systems are more complex to reason about and require strong monitoring and replay tooling.
- Regulatory compliance can impose strict controls that affect deployment topology and data handling.
- Without disciplined idempotency enforcement, duplicate payment requests can trigger duplicate downstream effects.

Mitigation strategies:
- define shared event schemas and versioning rules
- require idempotency and correlation keys across all entry points
- implement thorough reconciliation and audit checks
- use automated tests for payment flows and ledger integrity

---

## 12. Glossary

- Authorization: approval of a payment request by a network or provider.
- Clearing: process of mutual reconciliation of payment obligations.
- Event Backbone: integration layer for asynchronous message distribution.
- Idempotency: ensuring repeated requests do not cause repeated financial effects.
- Ledger: authoritative account of financial postings and balances.
- Orchestrator: coordination component for payment flow and state transitions.
- Reconciliation: checking that internal records match external financial outcomes.
- Settlement: final movement of funds between accounts or institutions.

---

## 13. Summary
AWS Payment Core is designed as a highly available, auditable, and regulatory-aware payment platform. Its architecture separates the synchronous, latency-sensitive authorization path from asynchronous downstream processing, enabling financial correctness without sacrificing responsiveness. The core foundation is a resilient AWS-native stack with EKS, Aurora PostgreSQL, MSK, and domain-driven service boundaries that support complex payment workflows.
