# Quick Start Guide - Financial Ledger Service

## Prerequisites

- Java 21+
- Docker Desktop running
- 8GB RAM available

## Start the Application

```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Wait 10 seconds for database to initialize

# 3. Run the application
./gradlew bootRun

# Application starts on http://localhost:8081
```

## Access Points

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Base:** http://localhost:8081/api
- **Health Check:** http://localhost:8081/actuator/health

## Test the API

### 1. Create Two Accounts

```bash
# Create Account A
curl -X POST http://localhost:8081/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "USD",
    "accountHolderName": "Alice"
  }'

# Save the accountId from response

# Create Account B
curl -X POST http://localhost:8081/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "USD",
    "accountHolderName": "Bob"
  }'
```

### 2. Deposit Money

```bash
curl -X POST http://localhost:8081/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACCOUNT_A_ID",
    "amount": 1000.00,
    "description": "Initial deposit"
  }'
```

### 3. Check Balance

```bash
curl http://localhost:8081/api/accounts/ACCOUNT_A_ID/balance
```

### 4. Transfer Money

```bash
curl -X POST http://localhost:8081/api/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "ACCOUNT_A_ID",
    "toAccountId": "ACCOUNT_B_ID",
    "amount": 250.00,
    "description": "Payment"
  }'
```

### 5. View Event History

```bash
curl http://localhost:8081/api/accounts/ACCOUNT_A_ID/events
```

### 6. Rebuild Projections (Admin)

```bash
curl -X POST http://localhost:8081/api/admin/projections/rebuild
```

## Key Features to Test

### Event Sourcing
- Every transaction creates immutable events
- View complete history via `/accounts/{id}/events`
- Events are never updated or deleted

### CQRS
- Write operations append events
- Read operations query materialized balances
- Balances can be rebuilt from events

### Transfer Saga
- Transfer creates multiple events atomically
- If transfer fails, automatic reversal
- Check transfer status via `/transactions/{id}`

### Optimistic Locking
- Try concurrent withdrawals from same account
- Only one succeeds, others get conflict error

## Test Concurrent Withdrawals

```bash
# In one terminal
curl -X POST http://localhost:8081/api/transactions/withdraw \
  -H "Content-Type: application/json" \
  -d '{"accountId": "ACCOUNT_ID", "amount": 600.00}'

# In another terminal (immediately)
curl -X POST http://localhost:8081/api/transactions/withdraw \
  -H "Content-Type: application/json" \
  -d '{"accountId": "ACCOUNT_ID", "amount": 600.00}'

# One should succeed, one should fail with concurrency error
```

## Stop Everything

```bash
# Stop application: Ctrl+C

# Stop PostgreSQL
docker-compose down

# Remove volumes (clean slate)
docker-compose down -v
```

## Troubleshooting

### Application won't start

Check if port 8081 is available:
```bash
netstat -ano | findstr :8081
```

### Database connection error

Ensure PostgreSQL is running:
```bash
docker-compose ps
```

### Check database

```bash
docker exec -it ledger-postgres psql -U postgres -d ledger_db -c "SELECT * FROM ledger_events;"
```

## Next Steps

1. Explore Swagger UI for all endpoints
2. Test event sourcing by viewing event history
3. Test CQRS by rebuilding projections
4. Test saga pattern with failed transfers
5. Test optimistic locking with concurrent operations

## Documentation

- [README.md](./README.md) - Project overview
- [DESIGN.md](./DESIGN.md) - Architecture decisions
