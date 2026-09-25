package com.biblioteca.catalog_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasResponse {
    private Long totalLibros;
    private Long totalEjemplares;
    private Long librosAgotados;
}