package com.biblioteca.catalog_service.repository;

import com.biblioteca.catalog_service.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Long> {
}