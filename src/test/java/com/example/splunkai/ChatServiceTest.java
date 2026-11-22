package com.example.splunkai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {
    @Test
    void mockChatReturnsLogEvidence() {
        ObjectMapper mapper = new ObjectMapper();
        SplunkService splunk = new SplunkService(
            "https://example.com:8089", "", "*", true, mapper);
        ChatService service = new ChatService(splunk, mapper, "", "gpt-4.1-mini");
        var response = service.answer("Why did requests fail?", "timeout", 30);
        assertEquals(3, response.eventCount());
        assertTrue(response.answer().contains("timeout"));
    }
}
