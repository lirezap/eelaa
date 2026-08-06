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
    "balance": -2000,
    "currency": "USD"
  },
  {
    "ledger": 1,
    "account": 1,
    "wallet": 2,
    "balance": 2000,
    "currency": "USD"
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
