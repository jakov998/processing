package com.betvictor.processing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.betvictor.processing.model.WordStats;
import com.betvictor.processing.service.WordStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class WordStatsServiceTest {

    private static final int PARAGRAPHS = 3;
    private static final String TYPE = "hipster-centric";

    @Mock
    private KafkaTemplate<String, WordStats> kafkaTemplate;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WordStatsService wordStatsService;

    @Test
    void testProcessText() {
        // Arrange

        List<String> mockParagraphs = List.of(
                "I love coffee and code.",
                "Code is life. Life is code.",
                "Spring Boot makes coding fun.");

        when(restTemplate.exchange(any(String.class), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(mockParagraphs));

        // Act
        WordStats result = wordStatsService.processText(PARAGRAPHS, TYPE);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.freq_word()).isEqualTo("code");

        BigDecimal expectedAvgSize = BigDecimal.valueOf(mockParagraphs.stream().mapToInt(String::length).average().orElse(0.0)).setScale(2, BigDecimal.ROUND_HALF_UP);
        assertThat(result.avg_paragraph_size()).isEqualTo(expectedAvgSize);

        verify(kafkaTemplate, times(1)).send(eq("words.processed"), any(WordStats.class));
    }

    @Test
    void testFetchParagraphs_EmptyResponse() {
        // Arrange
        when(restTemplate.exchange(any(String.class), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        // Act
        WordStats result = wordStatsService.processText(PARAGRAPHS, TYPE);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.freq_word()).isEqualTo("N/A");
        verify(kafkaTemplate, times(1)).send(anyString(), any(WordStats.class));
    }

    @Test
    void testFindMostFrequentWord() {
        // Arrange
        List<String> paragraphs = List.of("apple banana apple", "banana orange apple");

        // Act
        String mostFrequentWord = wordStatsService.findMostFrequentWord(paragraphs);

        // Assert
        assertThat(mostFrequentWord).isEqualTo("apple");
    }

    @Test
    void testSendToKafka() {
        // Arrange
        List<String> mockParagraphs = List.of(
                "I love coffee and code.",
                "Code is life. Life is code.",
                "Spring Boot makes coding fun.");

        when(restTemplate.exchange(any(String.class), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(mockParagraphs));

        // Act
        wordStatsService.processText(PARAGRAPHS, TYPE);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("words.processed"), any(WordStats.class));
    }
}
