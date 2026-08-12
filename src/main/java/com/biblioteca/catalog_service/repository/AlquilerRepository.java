package com.biblioteca.catalog_service.repository;

import com.biblioteca.catalog_service.model.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {
}