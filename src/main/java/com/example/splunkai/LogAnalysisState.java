package com.example.splunkai;

import org.bsc.langgraph4j.state.AgentState;
import java.util.Map;

/** Immutable graph snapshots: each node returns state updates. */
public class LogAnalysisState extends AgentState {
    public LogAnalysisState(Map<String, Object> data) { super(data); }
    public String question() { return this.<String>value("question").orElse(""); }
    public String query() { return this.<String>value("query").orElse(""); }
    public int minutes() { return this.<Integer>value("minutes").orElse(30); }
    public String logs() { return this.<String>value("logs").orElse("[]"); }
    public String analysis() { return this.<String>value("analysis").orElse(""); }
    public String answer() { return this.<String>value("answer").orElse(""); }
    public int eventCount() { return this.<Integer>value("eventCount").orElse(0); }
}
