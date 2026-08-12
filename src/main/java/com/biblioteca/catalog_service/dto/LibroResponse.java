package com.biblioteca.catalog_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibroResponse {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private Double precio;
    private Integer stock;
}