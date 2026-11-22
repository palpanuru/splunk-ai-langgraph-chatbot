# Splunk AI Search Chatbot

Java 21 Spring Boot Splunk REST proxy + Python FastAPI/LangGraph + browser chatbot.
Searches all indexes visible to the configured Splunk token by default (`SPLUNK_INDEX=*`). No service restriction.

## Run locally

```bash
cp .env.example .env
docker compose up --build
```

Open http://localhost:8000. Default `SPLUNK_MOCK=true` and `MOCK_AI=true` run without credentials. To connect Splunk, configure `SPLUNK_MOCK=false`, `SPLUNK_URL`, `SPLUNK_TOKEN`. To enable AI, set `MOCK_AI=false` and `OPENAI_API_KEY`.

## APIs

```bash
curl -G 'http://localhost:8080/api/logs' --data-urlencode 'q=timeout' --data-urlencode 'minutes=30'
curl -X POST http://localhost:8000/api/chat -H 'Content-Type: application/json' -d '{"message":"What errors are present?","search_text":"timeout","minutes":30}'
```

Search text is optional and treated as a literal, not arbitrary SPL. Search windows are 1–60 minutes, with at most 50 returned events. Logs are analyzed by a LangGraph search → analyze workflow.

**Security:** Local development prototype only. No user authentication or tenant isolation. Use a dedicated read-only least-privilege Splunk token. Redaction is illustrative, not comprehensive. Do not expose the application publicly without additional security controls.
