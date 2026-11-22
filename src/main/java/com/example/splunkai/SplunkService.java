package com.example.splunkai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
public class SplunkService {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String index;
    private final boolean mock;

    public SplunkService(@Value("${splunk.url}") String url,
                         @Value("${splunk.token:}") String token,
                         @Value("${splunk.index:*}") String index,
                         @Value("${splunk.mock:true}") boolean mock,
                         ObjectMapper mapper) {
        if (!index.matches("\\*|[A-Za-z0-9_-]{1,80}"))
            throw new IllegalArgumentException("Invalid Splunk index");
        if (!mock && (!url.startsWith("https://") || token.isBlank()))
            throw new IllegalArgumentException("Real Splunk requires HTTPS and token");
        this.index = index;
        this.mock = mock;
        this.mapper = mapper;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.client = RestClient.builder().baseUrl(url).requestFactory(factory)
            .defaultHeader("Authorization", "Bearer " + token).build();
    }

    public String searchLogs(String text, int minutes) {
        if (text == null || text.length() > 120 || text.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Search text must be at most 120 printable characters");
        if (minutes < 1 || minutes > 60)
            throw new IllegalArgumentException("Minutes must be between 1 and 60");
        if (mock) return """
            [{"_time":"2026-11-21T15:00:00Z","index":"main","service":"payment-service","level":"ERROR","message":"HikariPool connection timeout after 30000ms"},
             {"_time":"2026-11-21T15:01:00Z","index":"main","service":"order-service","level":"ERROR","message":"HTTP 503 from payment-service"},
             {"_time":"2026-11-21T15:02:00Z","index":"infra","service":"gateway","level":"WARN","message":"Upstream request timeout"}]
            """;
        // Never concatenate arbitrary SPL from a user or LLM. Quote search text as a literal.
        String literal = text.replace("\\", "\\\\").replace("\"", "\\\"");
        String spl = "search index=" + index + (text.isBlank() ? "" : " \"" + literal + "\"")
            + " | head 50 | fields _time index host source service level message _raw";
        String body = "search=" + URLEncoder.encode(spl, StandardCharsets.UTF_8)
            + "&earliest_time=-" + minutes + "m&latest_time=now&output_mode=json";
        String raw = client.post().uri("/services/search/v2/jobs/export")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body)
            .retrieve().body(String.class);
        if (raw == null || raw.isBlank()) return "[]";
        if (raw.length() > 250_000) throw new IllegalStateException("Splunk response too large");
        List<Map<String, String>> events = new ArrayList<>();
        try {
            for (String line : raw.split("\\R")) {
                if (line.isBlank()) continue;
                JsonNode event = mapper.readTree(line).path("result");
                if (!event.isObject()) continue;
                Map<String, String> clean = new LinkedHashMap<>();
                for (String field : List.of("_time", "index", "host", "source", "service", "level", "message")) {
                    String value = event.path(field).asText("");
                    if (field.equals("message") && value.isBlank()) value = event.path("_raw").asText("");
                    value = value.replaceAll("(?i)(password|token|secret|api[_-]?key)\\s*[:=]\\s*\\S+", "$1=[REDACTED]");
                    clean.put(field, value.substring(0, Math.min(value.length(), 1500)));
                }
                events.add(clean);
                if (events.size() == 50) break;
            }
            return mapper.writeValueAsString(events);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse Splunk response", ex);
        }
    }
}
