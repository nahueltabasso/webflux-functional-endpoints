package com.springboot.webflux.apirest.app.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.springboot.webflux.apirest.app.models.documents.Categoria;


public interface CategoriaRepository extends ReactiveMongoRepository<Categoria, String> {

}
