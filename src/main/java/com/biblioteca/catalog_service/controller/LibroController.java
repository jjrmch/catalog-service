package com.biblioteca.catalog_service.controller;

import com.biblioteca.catalog_service.dto.LibroRequest;
import com.biblioteca.catalog_service.dto.LibroResponse;
import com.biblioteca.catalog_service.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public List<LibroResponse> listarLibros() {
        return libroService.listarTodos();
    }

    @GetMapping("/{id}")
    public LibroResponse obtenerLibro(@PathVariable Long id) {
        return libroService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<LibroResponse> crearLibro(@Valid @RequestBody LibroRequest request) {
        LibroResponse creado = libroService.guardar(request);
        return ResponseEntity.status(201).body(creado);
    }

    @PutMapping("/{id}")
    public LibroResponse actualizarLibro(@PathVariable Long id, @Valid @RequestBody LibroRequest request) {
        return libroService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}