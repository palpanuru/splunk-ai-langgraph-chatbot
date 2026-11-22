package com.example.splunkai;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class IncidentController {
    private final SplunkService splunk;
    public IncidentController(SplunkService splunk) { this.splunk = splunk; }
    @GetMapping("/logs")
    public Map<String, String> logs(@RequestParam(defaultValue = "") String q,
                                    @RequestParam(defaultValue = "30") int minutes) {
        return Map.of("logs", splunk.searchLogs(q, minutes));
    }
}
