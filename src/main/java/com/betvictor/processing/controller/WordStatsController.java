package com.betvictor.processing.controller;

import com.betvictor.processing.model.WordStats;
import com.betvictor.processing.service.WordStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/betvictor")
@RequiredArgsConstructor
public class WordStatsController {

    private final WordStatsService service;

    @GetMapping("/text")
    public WordStats getText(@RequestParam(name = "p") int paragraphs, @RequestParam(name = "t") String type) {
        if (paragraphs <= 0 || !isValidType(type)) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        return service.processText(paragraphs, type);
    }

    private boolean isValidType(String length) {
        return Set.of("hipster-centric", "hipster-latin").contains(length);
    }
}