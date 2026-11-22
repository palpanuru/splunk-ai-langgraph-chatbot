package com.example.splunkai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Actual LangGraph4j graph: validate -> search_logs -> analyze -> report.
 * Graph is compiled once and safely reused across independent invocations.
 */
@Service
public class LogAnalysisGraph {
    private final CompiledGraph<LogAnalysisState> graph;

    public LogAnalysisGraph(SplunkService splunk, ChatService ai, ObjectMapper mapper)
            throws GraphStateException {
        graph = new StateGraph<>(LogAnalysisState::new)
            .addNode("validate", node_async(state -> {
                if (state.question().isBlank() || state.question().length() > 1000)
                    throw new IllegalArgumentException("Invalid question");
                return Map.<String, Object>of("validated", true);
            }))
            .addNode("search_logs", node_async(state -> {
                String logs = splunk.searchLogs(state.query(), state.minutes());
                try {
                    var events = mapper.readTree(logs);
                    if (!events.isArray()) throw new IllegalStateException("Expected log array");
                    return Map.<String, Object>of("logs", logs, "eventCount", events.size());
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new IllegalStateException("Invalid Splunk log data", e);
                }
            }))
            .addNode("analyze", node_async(state ->
                Map.<String, Object>of("analysis",
                    ai.analyzeLogs(state.question(), state.logs(), state.eventCount()))))
            .addNode("report", node_async(state ->
                Map.<String, Object>of("answer", state.analysis())))
            .addEdge(START, "validate")
            .addEdge("validate", "search_logs")
            .addEdge("search_logs", "analyze")
            .addEdge("analyze", "report")
            .addEdge("report", END)
            .compile();
    }

    public ChatController.ChatResponse run(String question, String query, int minutes) {
        var result = graph.invoke(Map.of(
            "question", question,
            "query", query,
            "minutes", minutes
        ));
        var state = result.orElseThrow(() ->
            new IllegalStateException("LangGraph returned no result"));
        return new ChatController.ChatResponse(state.answer(), state.eventCount());
    }
}
