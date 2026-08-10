package com.biblioteca.catalog_service.service;

import com.biblioteca.catalog_service.model.Libro;
import com.biblioteca.catalog_service.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibroService {

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public List<Libro> listarTodos() {
        return libroRepository.findAll();
    }

    public Libro guardar(Libro libro) {
        return libroRepository.save(libro);
    }

    public Optional<Libro> buscarPorId(Long id) {
        return libroRepository.findById(id);
    }

    public Libro actualizar(Long id, Libro datosNuevos) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado con id: " + id));

        libro.setTitulo(datosNuevos.getTitulo());
        libro.setAutor(datosNuevos.getAutor());
        libro.setIsbn(datosNuevos.getIsbn());
        libro.setPrecio(datosNuevos.getPrecio());
        libro.setStock(datosNuevos.getStock());

        return libroRepository.save(libro);
    }

    public void eliminar(Long id) {
        libroRepository.deleteById(id);
    }
}