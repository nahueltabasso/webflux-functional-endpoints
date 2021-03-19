package com.springboot.webflux.apirest.app.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.springboot.webflux.apirest.app.models.documents.Producto;


public interface ProductoRepository extends ReactiveMongoRepository<Producto, String> {

}
