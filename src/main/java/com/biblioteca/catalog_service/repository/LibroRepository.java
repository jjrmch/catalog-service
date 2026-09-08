package com.biblioteca.catalog_service.repository;

import com.biblioteca.catalog_service.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titulo, String autor, String isbn);

    Optional<Libro> findByIsbn(String isbn);

    @Query("SELECT COALESCE(SUM(l.stock), 0) FROM Libro l")
    Long sumarStock();

    long countByStock(Integer stock);
}