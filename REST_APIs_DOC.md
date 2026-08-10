# REST APIs Documentation

## HTTP Server Enablement

By default, the eelaa server listens on a TCP port. You should use a client TCP socket to connect to the server and send
the specified binary frames to receive responses. Since communicating over raw TCP can be complex, the server can also
be started in HTTP mode, allowing clients to interact with it using standard HTTP requests. To enable the HTTP server,
set the `SERVERS_TCP_HTTP_SERVER_CONFIG_ENABLED=true` environment variable before starting the server.

---

## Error Handling

All HTTP responses with status code not in 2xx range (exp, 4xx and 5xx) are considered error response that includes both
`code` and `message` fields in response body. The table of all possible errors is reachable at the end of the current
file.

#### Example Error Response:

```json
{
  "code": "resource.not_found",
  "message": "requested resource not found"
}
```

---

## Ping

### Endpoint

`POST /messages?numericType=1`

### Description

Pings HTTP server to check whether it is ready to handle incoming HTTP requests or not.

### Headers

| Header       | Required | Description      |
|--------------|----------|------------------|
| Content-Type | Yes      | application/json |

### Request Body

```json
{}
```

### Success Response (200)

---

## Fetch Account

### Endpoint

`POST /messages?numericType=100`

### Description

Fetches an account and all of its related wallets.

### Headers

| Header       | Required | Description      |
|--------------|----------|------------------|
| Content-Type | Yes      | application/json |

### Request Body

```json
{
  "sequenceId": 1,
  "ts": "1785862936000",
  "data": {
    "ledger": 1,
    "account": 1
  }
}
```

### Success Response (200)

```json
[
  {
    "ledger": 1,
    "account": 1,
    "wallet": 1,
    "currency": "USD",
    "balance": -2000
  },
  {
    "ledger": 1,
    "account": 1,
    "wallet": 2,
    "currency": "USD",
    "balance": 2000
  }
]
```

### Notes

- All requests sent by an HTTP client's connection must include a unique `sequenceId`.
- All requests sent by an HTTP client must include a valid current timestamp in `ts` field.
- Two previous mentioned fields exists for security/idempotency reasons and can be disabled using environment variables.
- A `ledger` is a collection of accounts. A ledger has a positive integer identifier.
- An `account` is a collection of wallets. An account has a positive integer identifier.
- A wallet is a currency/balance pair. A wallet has a non-zero integer identifier.

---

## Fetch Wallet

### Endpoint

`POST /messages?numericType=200`

### Description

Fetches a specific wallet of an account.

### Headers

| Header       | Required | Description      |
|--------------|----------|------------------|
| Content-Type | Yes      | application/json |

### Request Body

```json
{
  "sequenceId": 5,
  "ts": "1786005152532",
  "data": {
    "ledger": 1,
    "account": 1,
    "wallet": 1
  }
}
```

### Success Response (200)

```json
{
  "ledger": 1,
  "account": 1,
  "wallet": 1,
  "currency": "USD",
  "balance": -2000
}
```

### Notes

- All requests sent by an HTTP client's connection must include a unique `sequenceId`.
- All requests sent by an HTTP client must include a valid current timestamp in `ts` field.
- Two previous mentioned fields exists for security/idempotency reasons and can be disabled using environment variables.
- A `ledger` is a collection of accounts. A ledger has a positive integer identifier.
- An `account` is a collection of wallets. An account has a positive integer identifier.
- A `wallet` is a currency/balance pair. A wallet has a non-zero integer identifier.

---

## Submit a Batch of Transfers

### Endpoint

`POST /messages?numericType=300`

### Description

Submits a batch of transfers, should include at least a single transfer.

### Headers

| Header       | Required | Description      |
|--------------|----------|------------------|
| Content-Type | Yes      | application/json |

### Request Body

```json
{
  "sequenceId": 1,
  "ts": "1786269247957",
  "data": [
    {
      "ledger": 1,
      "sourceAccount": 1,
      "sourceWallet": 1,
      "destinationAccount": 1,
      "destinationWallet": 2,
      "id": "1786269247998:1",
      "currency": "USD",
      "amount": 1000,
      "maxOverdraftAmount": 10000,
      "metadata": "{\"key\":\"value\"}"
    },
    {
      "ledger": 1,
      "sourceAccount": 1,
      "sourceWallet": 1,
      "destinationAccount": 1,
      "destinationWallet": 2,
      "id": "1786269248923:2",
      "currency": "USD",
      "amount": 5000,
      "maxOverdraftAmount": 10000,
      "metadata": "{\"anotherKey\":\"anotherValue\"}"
    }
  ]
}
```

### Success Response (200)

```json
[]
```

### Notes

- All requests sent by an HTTP client's connection must include a unique `sequenceId`.
- All requests sent by an HTTP client must include a valid current timestamp in `ts` field.
- Two previous mentioned fields exists for security/idempotency reasons and can be disabled using environment variables.
- `ledger` is the ledger id that a specific transfer belongs to.
- `sourceAccount` is the source account id of the transfer.
- `sourceWallet` is the source wallet id of the source account.
- `destinationAccount` is the destination account id of the transfer.
- `destinationWallet` is the destination wallet id of the destination account.
- `id` is a two part transfer id, should be built using `timestamp:string` pattern. This field can be used for inquiry.
- `currency` is the currency of the wallets.
- `amount` is the amount of the transfer.
- `maxOverdraftAmount` determines the maximum amount of overdraft value of the sourceWallet when processing the
  transfer.
- `metadata` extra informational data attached to this transfer in raw string format.
- If one or more items in the batch failed, the response includes the fail reason of failed transfers; like in:

```json
[
  {
    "id": "1786269458479:1",
    "reason": "balance.not_enough"
  }
]
```

---

## Submit an Atomic (All-or-None) Batch of Transfers

### Endpoint

`POST /messages?numericType=301`

### Description

Submits an atomic batch of transfers, that means all items in a batch must be succeeded or failed.

### Headers

| Header       | Required | Description      |
|--------------|----------|------------------|
| Content-Type | Yes      | application/json |

### Request Body

```json
{
  "sequenceId": 1,
  "ts": "1786269247957",
  "data": [
    {
      "ledger": 1,
      "sourceAccount": 1,
      "sourceWallet": 1,
      "destinationAccount": 1,
      "destinationWallet": 2,
      "id": "1786269247998:1",
      "currency": "USD",
      "amount": 1000,
      "maxOverdraftAmount": 10000,
      "metadata": "{\"key\":\"value\"}"
    },
    {
      "ledger": 1,
      "sourceAccount": 1,
      "sourceWallet": 1,
      "destinationAccount": 1,
      "destinationWallet": 2,
      "id": "1786269248923:2",
      "currency": "USD",
      "amount": 5000,
      "maxOverdraftAmount": 10000,
      "metadata": "{\"anotherKey\":\"anotherValue\"}"
    }
  ]
}
```

### Success Response (200)

```json
[]
```

### Notes

- All requests sent by an HTTP client's connection must include a unique `sequenceId`.
- All requests sent by an HTTP client must include a valid current timestamp in `ts` field.
- Two previous mentioned fields exists for security/idempotency reasons and can be disabled using environment variables.
- `ledger` is the ledger id that a specific transfer belongs to.
- `sourceAccount` is the source account id of the transfer.
- `sourceWallet` is the source wallet id of the source account.
- `destinationAccount` is the destination account id of the transfer.
- `destinationWallet` is the destination wallet id of the destination account.
- `id` is a two part transfer id, should be built using `timestamp:string` pattern. This field can be used for inquiry.
- `currency` is the currency of the wallets.
- `amount` is the amount of the transfer.
- `maxOverdraftAmount` determines the maximum amount of overdraft value of the sourceWallet when processing the
  transfer.
- `metadata` extra informational data attached to this transfer in raw string format.
- If one or more items failed, the entire batch is failed and the response includes the fail reason of the first failed
  item, like in:

```json
[
  {
    "id": "1786269458479:1",
    "reason": "balance.not_enough"
  }
]
```

---

## Transfer Inquiry

### Endpoint

`POST /messages?numericType=400`

### Description

Inquiries a specific transfer using its id.

### Headers

| Header       | Required | Description      |
|--------------|----------|------------------|
| Content-Type | Yes      | application/json |

### Request Body

```json
{
  "sequenceId": 10,
  "ts": "1786273336750",
  "data": {
    "ledger": 1,
    "id": "1786273315084:1"
  }
}
```

### Success Response (200)

```json
{
  "ledger": 1,
  "sourceAccount": 1,
  "sourceWallet": 1,
  "destinationAccount": 1,
  "destinationWallet": 2,
  "id": "1786273315084:1",
  "currency": "BTC",
  "amount": 1000,
  "maxOverdraftAmount": 10000,
  "metadata": "{\"key\":\"value\"}",
  "sourceWalletNewBalance": -2000,
  "destinationWalletNewBalance": 2000,
  "ts": 1786273317518
}
```

### Notes

- All requests sent by an HTTP client's connection must include a unique `sequenceId`.
- All requests sent by an HTTP client must include a valid current timestamp in `ts` field.
- Two previous mentioned fields exists for security/idempotency reasons and can be disabled using environment variables.
- `ledger` is the ledger id that the inquired transfer belongs to.
- `id` is the transfer id to be inquiry.

---

## Possible Errors

| code                 | message                                       |
|----------------------|-----------------------------------------------|
| method.not_supported | http method not supported                     |
| handler.not_found    | http handler not found                        |
| content_type.invalid | empty or invalid content type header provided |
| sequenceId.invalid   | invalid sequence id provided                  |
| ts.invalid           | invalid timestamp provided                    |
| data.is_required     | data object in the json body is required      |
| resource.not_found   | requested resource not found                  |
| server.error         | internal server error occurred                |
