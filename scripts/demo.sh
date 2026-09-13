#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
MOCK_BACKBONE_URL="${MOCK_BACKBONE_URL:-http://localhost:8081}"

echo "Creating demo payment..."
PAYMENT_RESPONSE=$(curl -sS -X POST "$BASE_URL/api/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "demo-merchant",
    "customerId": "demo-customer",
    "reference": "ref-demo-001",
    "amount": 12.34,
    "currency": "USD",
    "paymentMethod": "CARD",
    "channel": "WEB",
    "idempotencyKey": "idem-demo-001"
  }')

echo "$PAYMENT_RESPONSE"
PAYMENT_ID=$(echo "$PAYMENT_RESPONSE" | python3 -c 'import sys, json; print(json.load(sys.stdin)["id"])')

echo "Authorizing payment $PAYMENT_ID..."
curl -sS -X POST "$BASE_URL/api/payments/$PAYMENT_ID/authorize"

echo

echo "Mock backbone events:"
curl -sS "$MOCK_BACKBONE_URL/events"
