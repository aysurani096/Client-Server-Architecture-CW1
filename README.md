# MLOps Pipeline Management API

A RESTful API built with JAX-RS (Jersey 2.41) and Grizzly for managing Machine Learning Workspaces, Models, and Evaluation Metrics. Built as part of the 5COSC022W Client-Server Architectures module at the University of Westminster.

---

## How to Build and Run

**Prerequisites:** Java 11 or higher, Maven 3.6+

**Step 1 – Clone the repo and navigate to the project folder:**
```bash
cd mlops-api
```

**Step 2 – Build the fat JAR:**
```bash
mvn clean package
```

**Step 3 – Run the server:**
```bash
java -jar target/mlops-api-1.0-SNAPSHOT.jar
```

The server starts at: `http://localhost:8080/api/v1`

Press `Ctrl+C` to stop.

---

## Project Structure

```
src/main/java/com/mlops/
├── Main.java                          # Grizzly server entry point
├── MLOpsApplication.java              # JAX-RS Application config (@ApplicationPath)
├── model/
│   ├── MLWorkspace.java
│   ├── MachineLearningModel.java      # includes ModelStatus enum
│   └── EvaluationMetric.java
├── store/
│   └── DataStore.java                 # in-memory singleton (HashMap-based)
├── resource/
│   ├── DiscoveryResource.java         # GET /api/v1/
│   ├── WorkspaceResource.java         # /api/v1/workspaces
│   ├── ModelResource.java             # /api/v1/models
│   └── MetricSubResource.java         # sub-resource for /api/v1/models/{id}/metrics
├── exception/
│   ├── WorkspaceNotEmptyException.java
│   ├── WorkspaceNotEmptyExceptionMapper.java      → HTTP 409
│   ├── LinkedWorkspaceNotFoundException.java
│   ├── LinkedWorkspaceNotFoundExceptionMapper.java → HTTP 422
│   ├── ModelDeprecatedException.java
│   ├── ModelDeprecatedExceptionMapper.java        → HTTP 403
│   └── GlobalExceptionMapper.java                 → HTTP 500 (catch-all)
└── filter/
    └── LoggingFilter.java             # logs every request and response
```

---

## API Endpoints

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | /api/v1/ | Discovery – version info and resource map | 200 |
| GET | /api/v1/workspaces | List all workspaces | 200 |
| POST | /api/v1/workspaces | Create a new workspace | 201 |
| GET | /api/v1/workspaces/{id} | Get a specific workspace | 200 / 404 |
| DELETE | /api/v1/workspaces/{id} | Delete workspace (blocked if has models) | 204 / 409 / 404 |
| GET | /api/v1/models | List all models (optional `?status=` filter) | 200 |
| POST | /api/v1/models | Register a new model (validates workspaceId) | 201 / 422 |
| GET | /api/v1/models/{id}/metrics | List evaluation metrics for a model | 200 / 404 |
| POST | /api/v1/models/{id}/metrics | Add an evaluation metric | 201 / 403 / 404 |

---

## Sample curl Commands

### 1. Create a Workspace
```bash
curl -X POST http://localhost:8080/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -d '{"teamName":"Computer Vision Lab","storageQuotaGb":500}'
```

### 2. Register a Model (replace `WS-XXXXXX` with your workspace ID)
```bash
curl -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"PyTorch","status":"TRAINING","latestAccuracy":0.0,"workspaceId":"WS-XXXXXX"}'
```

### 3. Filter Models by Status
```bash
curl "http://localhost:8080/api/v1/models?status=TRAINING"
```

### 4. Add an Evaluation Metric (replace `MOD-XXXXXX` with your model ID)
```bash
curl -X POST http://localhost:8080/api/v1/models/MOD-XXXXXX/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.94}'
```

### 5. Try to Delete a Workspace with Models (expect 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/api/v1/workspaces/WS-XXXXXX
```

---

## Design Decisions

All data is stored in-memory using `HashMap` and `ArrayList` inside a singleton `DataStore` class. Data does not persist after the server is stopped, which is fine for this coursework. In a real production system you'd replace this with a proper database.

The server generates all resource IDs using `UUID.randomUUID()` — clients do not pass their own IDs.

---

## Answers to Coursework Questions

### Part 1.1 – Role of MessageBodyWriter / Jackson in JSON serialisation

When a resource method returns a Java object (like `MLWorkspace`), JAX-RS does not know how to turn it into a JSON string by itself. It needs a `MessageBodyWriter` implementation to do that conversion. Jackson's JAX-RS provider (`JacksonFeature`) registers itself as a `MessageBodyWriter<Object>`. When JAX-RS sees that the response should be `application/json`, it looks for a compatible `MessageBodyWriter` and calls Jackson's writer. Jackson then uses reflection to inspect the object's fields (via getters) and converts them into a JSON string. The `MessageBodyWriter` is essentially the bridge between Java objects and the HTTP response body.

### Part 1.2 – What does 'stateless' mean in REST, and why does it help with scaling?

Stateless means every HTTP request must contain all the information the server needs to process it — the server does not store any session or client state between requests. Each request is completely self-contained. This makes horizontal scaling much easier because any server in a cluster can handle any request — there's no need for requests from the same client to be routed to the same server (no sticky sessions). You can add or remove servers without worrying about which server holds which client's session data. In cloud environments this is especially useful because you can spin up more instances under load and requests just get distributed evenly.

### Part 2.1 – How do Cache-Control headers improve performance?

If the `GET /api/v1/workspaces` response includes a `Cache-Control: max-age=60` header, the client's browser or any intermediate proxy will cache the response for 60 seconds and won't send another request until that time expires. This reduces the number of requests hitting the server, which lowers processing load. For data that doesn't change frequently (like a list of workspaces), this can significantly reduce server traffic. You could also use `ETag` headers combined with `If-None-Match` so the client only downloads the full response if the data has actually changed — if it hasn't, the server sends back a `304 Not Modified` with no body, saving bandwidth.

### Part 2.2 – Which HTTP method to check if a workspace exists without downloading the body?

The `HEAD` method. `HEAD` is identical to `GET` but the server only returns the response headers, not the body. So if the workspace exists, you get a `200 OK` with all the normal headers (including `Content-Type`, `Content-Length`, etc.) but no JSON body is sent over the wire. If it doesn't exist, you get a `404`. This is useful for existence checks, validating links, or checking when a resource was last modified — all without the bandwidth cost of downloading the actual data.

### Part 3.1 – Why should the server generate the model ID rather than the client?

There are two main reasons. First, **security**: if a client can pass any ID they want, they might try to overwrite an existing resource by passing a known ID, or try IDs like `admin`, `null`, or empty strings that could cause unexpected behaviour. The server using `UUID.randomUUID()` guarantees uniqueness and prevents ID collision attacks. Second, **data integrity**: the server is the authoritative source of truth. If two clients both try to create a resource with the same ID at the same time, the server can detect and reject duplicates. If clients generate their own IDs, there's no reliable way to enforce uniqueness, especially in a distributed system.

### Part 3.2 – Why does URL encoding matter for query parameters with spaces or special characters?

URLs can only contain a limited set of ASCII characters. Spaces, `&`, `=`, and other special characters have specific meaning in a URL (for example `&` separates query parameters, `=` separates key from value). If you use them literally in a value, the URL parser will misinterpret them. So `?framework=Scikit Learn & Tools` would be parsed as two separate query params: `framework=Scikit Learn ` and `Tools` (with no value). URL encoding replaces unsafe characters with their percent-encoded equivalents: space becomes `%20` (or `+`), `&` becomes `%26`. The correctly encoded URL would be `?framework=Scikit%20Learn%20%26%20Tools`. Most HTTP clients (like curl or browsers) handle this automatically, but it's important to understand why it's necessary.

### Part 4.1 – Class-level vs method-level @Produces annotation

Putting `@Produces(MediaType.APPLICATION_JSON)` at the class level means every method in that class inherits it — you don't have to repeat it on each method. This keeps the code cleaner when all methods produce the same content type. If a specific method needs to return something different (say XML or plain text), you put `@Produces(MediaType.APPLICATION_XML)` on that method, and it overrides the class-level annotation just for that method. JAX-RS always prefers the more specific (method-level) annotation when both are present. So the class-level acts as a sensible default and the method-level lets you override individual cases.

### Part 5.2 – Why must validation failures return 4xx rather than 5xx?

HTTP status codes are grouped by who is responsible for the error. `5xx` codes mean something went wrong on the server — it's the server's fault, and the client couldn't have done anything differently. `4xx` codes mean the client made a bad request — the request itself is the problem. When a client sends a `workspaceId` that doesn't exist, the server processed the request perfectly fine, found no matching workspace, and correctly rejected it. The server is working as intended. The fault is on the client side — they sent invalid data. Using `5xx` here would be misleading: it would suggest the server has a bug or internal issue, and would prevent clients from knowing they need to fix their request.

### Part 5.5 – Two pieces of useful HTTP metadata from request/response contexts

From `ContainerRequestContext`:
1. **Request URI** (`getUriInfo().getRequestUri()`) — tells you exactly which endpoint was called, including query parameters. Essential for matching a log entry to a specific API call when debugging.
2. **HTTP Method** (`getMethod()`) — knowing whether it was a `GET`, `POST`, `DELETE`, etc. is critical for understanding what operation failed and why.

From `ContainerResponseContext`:
1. **HTTP status code** (`getStatus()`) — immediately tells you if the operation succeeded or failed and what kind of failure it was (4xx vs 5xx).
2. **Response headers** (`getHeaders()`) — useful for debugging issues with content negotiation, CORS, caching, or checking the `Location` header on `201 Created` responses to verify the correct resource URL was returned.
#   C l i e n t - S e r v e r - A r c h i t e c t u r e - C W 1  
 