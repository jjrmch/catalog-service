package com.biblioteca.catalog_service;

import com.biblioteca.catalog_service.exception.StockInsuficienteException;
import com.biblioteca.catalog_service.model.Libro;
import com.biblioteca.catalog_service.repository.LibroRepository;
import com.biblioteca.catalog_service.service.LibroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private LibroService libroService;

    @Test
    void conPeticionesConcurrentesElStockNuncaQuedaNegativo() throws Exception {
        Libro libro = libroRepository.save(
                new Libro(null, "Libro concurrente", "Test", "isbn-" + UUID.randomUUID(), 10.0, 10));

        int peticiones = 20;
        ExecutorService pool = Executors.newFixedThreadPool(peticiones);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        for (int i = 0; i < peticiones; i++) {
            resultados.add(pool.submit(() -> {
                salida.await();
                try {
                    libroService.ajustarStock(libro.getId(), -1);
                    return true;
                } catch (StockInsuficienteException e) {
                    return false;
                }
            }));
        }

        salida.countDown();

        long exitos = 0;
        long fallos = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get(30, TimeUnit.SECONDS)) {
                exitos++;
            } else {
                fallos++;
            }
        }
        pool.shutdown();

        assertEquals(10, exitos, "solo deben venderse los 10 ejemplares disponibles");
        assertEquals(10, fallos, "las otras 10 peticiones deben rechazarse por stock");
        assertEquals(0, libroRepository.findById(libro.getId()).orElseThrow().getStock(),
                "el stock final debe ser exactamente 0, nunca negativo");
    }
}
