package com.betvictor.processing.model;

import java.math.BigDecimal;

public record WordStats(
        String freq_word,
        BigDecimal avg_paragraph_size,
        BigDecimal avg_paragraph_processing_time,
        BigDecimal total_processing_time
) {}
