# AWS Core Payment

[![Java 25](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![AWS](https://img.shields.io/badge/AWS-Cloud_Native-FF9900.svg)](https://aws.amazon.com/)
[![Architecture](https://img.shields.io/badge/Architecture-arc42-8A2BE2.svg)](docs/arc42.md)
[![Status](https://img.shields.io/badge/Status-Architecture_Scaffold-yellow.svg)](README.md)

AWS Core Payment is a Java-based, cloud-native payment processing platform designed for modern financial services workloads. The project models a payment orchestration system that can handle customer payment initiation, risk checks, routing to payment networks, ledger posting, and downstream lifecycle processing such as clearing, settlement, reconciliation, and notifications.

## What this project is about

This repository is intended to represent the foundation of a banking-grade payment platform built around the following ideas:

- Synchronous processing for customer-facing authorization flows
- Asynchronous event-driven processing for downstream lifecycle tasks
- ISO 8583, ISO 20022, and REST integration patterns
- Double-entry ledger accounting for financial integrity
- Strong idempotency and correlation tracking
- Cloud-native deployment on AWS using Kubernetes, Aurora PostgreSQL, and Kafka
- High availability, auditability, and operational observability

## Architecture overview

The project follows the same high-level architecture described in the docs:

- Payment Gateway: validates requests, enforces rate limits, and ensures idempotent processing
- Payment Orchestrator: coordinates the payment workflow across risk, routing, and provider interactions
- Adapter Layer: connects to ISO 8583, ISO 20022, and REST-based payment providers
- Ledger Service: maintains authoritative accounting state and double-entry bookkeeping
- Event Backbone: emits payment lifecycle events for clearing, settlement, notifications, and reconciliation

## Technology direction

The intended technical stack is:

- Java 25
- Spring Boot
- Maven
- Amazon EKS for orchestration and service deployment
- Aurora PostgreSQL for transactional and ledger data
- Amazon MSK for event-driven messaging
- AWS-native security, networking, and observability patterns

## Documentation

The project includes architecture documentation in the docs folder:

- docs/arch.md — high-level business and platform architecture
- docs/arc42.md — structured architecture description aligned to the arc42 template

## Current repository status

This repo is currently a scaffold for the platform and does not yet contain the full production implementation. It is intended as a foundation for building domain-driven payment services and related infrastructure components.

## Demo (Docker Compose)

A lightweight demo uses mocked external services so the platform can be demonstrated offline.

1. Start the demo services:

   docker-compose up --build

   This starts:
   - mock-backbone (Flask) on http://localhost:8081
   - app (Spring Boot) on http://localhost:8080

2. Create a payment (example):

   curl -s -X POST http://localhost:8080/api/payments \
     -H "Content-Type: application/json" \
     -d '{"merchantId":"demo-merchant","customerId":"demo-customer","reference":"ref-demo-1","amount":12.34,"currency":"USD","paymentMethod":"CARD","channel":"WEB","idempotencyKey":"idem-demo-1"}' | jq

3. Trigger authorization processing:

   # Replace <paymentId> with the id returned by the create call
   curl -s -X POST http://localhost:8080/api/payments/<paymentId>/authorize | jq

4. Inspect events from the mock backbone:

   curl http://localhost:8081/events | sed -n '1,200p'

Notes:
- The mock backbone retains events in memory only while running.
- To switch the app to use the remote mock backbone, set messaging.backbone=remote in application.yml or via environment variables.



## Suggested next steps

A real implementation would typically include:

1. Domain model for payments, ledger entries, and status transitions
2. Payment gateway and REST API endpoints
3. Idempotency store and correlation tracking
4. Orchestration workflow engine
5. Risk and routing rules engine
6. Adapter implementations for ISO 8583 and ISO 20022
7. Ledger persistence and reconciliation logic
8. Kafka event producers and consumers
9. Deployment manifests and CI/CD automation for AWS

## License

This project is currently a seed/architecture project and does not yet define a formal license.
