package com.example.splunkai;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SplunkServiceTest {
  private final SplunkService service = new SplunkService("https://example.com:8089", "", "*", true, new ObjectMapper());
  @Test void mockSearchWorksWithoutService() { assertTrue(service.searchLogs("timeout", 30).contains("HikariPool")); }
  @Test void rejectsLongSearch() { assertThrows(IllegalArgumentException.class, () -> service.searchLogs("x".repeat(121), 30)); }
  @Test void rejectsUnboundedTime() { assertThrows(IllegalArgumentException.class, () -> service.searchLogs("", 500)); }
}
