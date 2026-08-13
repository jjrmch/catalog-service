package com.biblioteca.catalog_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AjusteStockRequest {

    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;   // positivo = añadir stock, negativo = restar
}