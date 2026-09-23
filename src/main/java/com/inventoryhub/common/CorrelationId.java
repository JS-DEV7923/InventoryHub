package com.inventoryhub.common;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

public final class CorrelationId {
    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }

    public static String currentOrNew() {
        return Optional.ofNullable(MDC.get(MDC_KEY)).orElseGet(() -> {
            String generated = UUID.randomUUID().toString();
            MDC.put(MDC_KEY, generated);
            return generated;
        });
    }
}
