# Splunk AI Chatbot — Java 21 + Spring Boot

Single-process Java application: HTML/JavaScript chat UI, REST API, Splunk search and AI log analysis. **No Python, FastAPI or LangGraph runtime.**

## Start locally

```bash
cp .env.example .env
docker compose up --build -d
```

Or run `mvn spring-boot:run` in IntelliJ / terminal with Java 21.

- **Chatbot UI:** http://localhost:8080/
- **Spring Boot Swagger:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Log search:** GET http://localhost:8080/api/logs?q=timeout&minutes=30
- **Chat API:** POST http://localhost:8080/api/chat

Example:

```bash
curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d '{"message":"What caused the failures?","searchText":"timeout","minutes":30}'
```

## Configure Splunk and AI

```dotenv
SPLUNK_MOCK=false
SPLUNK_URL=https://splunk.example.com:8089
SPLUNK_TOKEN=your-read-only-token
SPLUNK_INDEX=*
OPENAI_API_KEY=your-api-key
OPENAI_MODEL=gpt-4.1-mini
```

By default, `SPLUNK_MOCK=true` returns sample events. Without an OpenAI key, the Java chatbot returns a simple event summary. It does **not** perform model-based root-cause analysis in this mode.

The search text is optional and treated as a literal, not executable SPL. Search windows are 1–60 minutes, and at most 50 events are returned. `SPLUNK_INDEX=*` searches indexes accessible to the Splunk token; no service name is required.

**Security note:** Local development example, not production hardened. Add authentication/authorization, audit logging, comprehensive redaction and rate limits before exposing it. Never commit `.env` or credentials. The repo is currently public.
