package com.betvictor.processing;

import com.betvictor.processing.model.WordStats;
import com.betvictor.processing.service.WordStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class WordStatsControllerTest {

    private static final String MOST_FREQUENT_WORD = "word1";
    private static final BigDecimal AVG_PARAGRAPH_SIZE = BigDecimal.valueOf(460.50).setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal AVG_PROCESSING_TIME = BigDecimal.valueOf(181.55).setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal TOTAL_PROCESSING_TIME = BigDecimal.valueOf(1810.00).setScale(2, RoundingMode.HALF_UP);

    private static final String PARAGRAPHS = "3";
    private static final String TYPE = "hipster-centric";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WordStatsService wordStatsService;

    @Test
    public void testGetWordStats() throws Exception {

        WordStats wordStats = new WordStats(MOST_FREQUENT_WORD, AVG_PARAGRAPH_SIZE, AVG_PROCESSING_TIME, TOTAL_PROCESSING_TIME);

        when(wordStatsService.processText(anyInt(), anyString())).thenReturn(wordStats);

        mockMvc.perform(get("/betvictor/text")
                        .param("p", PARAGRAPHS)
                        .param("t", TYPE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json("""
                        {
                                "freq_word": "%s",
                                "avg_paragraph_size": %f,
                                "avg_paragraph_processing_time": %f,
                                "total_processing_time": %f
                        }""".formatted(MOST_FREQUENT_WORD, AVG_PARAGRAPH_SIZE, AVG_PROCESSING_TIME, TOTAL_PROCESSING_TIME)));
    }

}
