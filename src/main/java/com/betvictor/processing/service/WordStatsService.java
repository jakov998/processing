package com.betvictor.processing.service;
import com.betvictor.processing.model.WordStats;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WordStatsService {

    private final KafkaTemplate<String, WordStats> kafkaTemplate;
    private final RestTemplate restTemplate;
    private static final String API = "http://hipsum.co/api/?paras=%d&type=%s";
    private static final Logger logger = LoggerFactory.getLogger(WordStatsService.class);

    public WordStats processText(int paragraphs, String type) {
        logger.info("Starting text processing: paragraphs={}, type={}", paragraphs, type);

        long startTime = System.currentTimeMillis();
        List<String> paragraphList = fetchParagraphs(paragraphs, type);

        logger.info("Fetched {} paragraphs successfully.", paragraphList.size());

        String mostFrequentWord = findMostFrequentWord(paragraphList);

        BigDecimal avgSize = BigDecimal.valueOf(
                paragraphList.stream().mapToInt(String::length).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);

        long endTime = System.currentTimeMillis();

        BigDecimal totalTime = BigDecimal.valueOf(endTime - startTime).setScale(2, RoundingMode.HALF_UP);
        BigDecimal averageTime = totalTime.divide(BigDecimal.valueOf(paragraphList.size()), 2, RoundingMode.HALF_UP);

        WordStats response = new WordStats(mostFrequentWord, avgSize, averageTime, totalTime);

        logger.info("Text processing completed. Sending result to kafka: {}", response);

        kafkaTemplate.send("words.processed", response);

        return response;
    }

    private List<String> fetchParagraphs(int p, String type) {
        List<String> paragraphs = new ArrayList<>();
        for (int i = 1; i <= p; i++) {
            String url = String.format(API, i, type);
            logger.info("Fetching text from: {}", url);

            List<String> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {}).getBody();

            if (response != null && !response.isEmpty()) {
                paragraphs.addAll(response);
            } else {
                paragraphs.add("");
            }
        }
        return paragraphs;
    }

    public String findMostFrequentWord(List<String> paragraphs) {
        Map<String, Integer> frequency = new HashMap<>();
        paragraphs.stream()
                .filter(p -> !p.isBlank())
                .flatMap(p -> Arrays.stream(p.toLowerCase().split("\\W+")))
                .forEach(word -> frequency.put(word, frequency.getOrDefault(word, 0) + 1));

        return frequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }
}