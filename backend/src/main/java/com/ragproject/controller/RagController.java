package com.ragproject.controller;
import com.ragproject.service.RagQueryService;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagQueryService ragQueryService;

    public RagController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @PostMapping("/query")
    public Map<String, String> query(@RequestBody Map<String, Object> body) {
        String query = (String) body.get("query");
        int maxResults = (int) body.getOrDefault("maxResults", 3);
        String answer = ragQueryService.query(query, maxResults);
        
        
        Map<String, String> response = new HashMap<>();
        response.put("response", answer);
        return response;
       
    }
}