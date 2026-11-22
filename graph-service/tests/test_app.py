import os
os.environ["MOCK_AI"] = "true"
from fastapi.testclient import TestClient
import app

def test_health():
    assert TestClient(app.app).get("/health").status_code == 200

def test_request_has_no_service_field():
    payload = app.ChatRequest(message="What happened?", search_text="timeout", minutes=30)
    assert not hasattr(payload, "service")

def test_search_node(monkeypatch):
    class Response:
        def raise_for_status(self): pass
        def json(self): return {"logs": '[{"message":"timeout"}]'}
    class Client:
        def __init__(self, **kwargs): pass
        def __enter__(self): return self
        def __exit__(self, *args): pass
        def get(self, url, params):
            assert params == {"q": "timeout", "minutes": 30}
            return Response()
    monkeypatch.setattr(app.httpx, "Client", Client)
    state = {"question":"why", "search_text":"timeout", "minutes":30,"history":[],"logs":[],"steps":[],"answer":""}
    result = app.graph.invoke(state)
    assert result["steps"] == ["search_logs", "analyze"]
    assert "timeout" in result["answer"]
