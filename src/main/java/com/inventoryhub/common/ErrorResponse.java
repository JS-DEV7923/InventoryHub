package com.inventoryhub.common;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        String correlationId,
        List<FieldErrorResponse> fieldErrors
) {
}
