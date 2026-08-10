package com.biblioteca.catalog_service.repository;

import com.biblioteca.catalog_service.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {
}