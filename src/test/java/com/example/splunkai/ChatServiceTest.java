package com.example.splunkai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {
    @Test
    void graphSearchesAndSummarizesMockLogs() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SplunkService splunk = new SplunkService(
            "https://example.com:8089", "", "*", true, mapper);
        ChatService ai = new ChatService(mapper, "", "gpt-4.1-mini");
        LogAnalysisGraph graph = new LogAnalysisGraph(splunk, ai, mapper);
        var response = graph.run("Why did requests fail?", "timeout", 30);
        assertEquals(3, response.eventCount());
        assertTrue(response.answer().contains("timeout"));
    }
}
