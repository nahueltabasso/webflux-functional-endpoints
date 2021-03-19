package com.springboot.webflux.apirest.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.springboot.webflux.apirest.app.models.documents.Categoria;
import com.springboot.webflux.apirest.app.models.documents.Producto;
import com.springboot.webflux.apirest.app.models.services.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {

	private static Logger logger = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);
	@Autowired
	private ProductoService productoService;
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria categoria1 = new Categoria("Electronico");
		Categoria categoria2 = new Categoria("Libros");
		Categoria categoria3 = new Categoria("Computacion");
		Categoria categoria4 = new Categoria("Celulares");

		Flux.just(categoria1, categoria2, categoria3, categoria4).flatMap(c -> productoService.saveCategoria(c))
				.doOnNext(c -> {
					logger.info("Categoria creada " + c.getId() + ", " + c.getNombre());
				})
				.thenMany(Flux.just(new Producto("Samsung TV 58´´ UHD", 75000.00, categoria1),
						new Producto("Iphone 11 64GB", 140000.00, categoria4),
						new Producto("Samsung Galaxy S21", 110000.00, categoria4),
						new Producto("Macbook Pro M1 256GB", 200000.00, categoria3),
						new Producto("Java SE 8 Certificate", 5000.00, categoria2),
						new Producto("Ipad Pro 64GB", 165000.00, categoria1),
						new Producto("Epson L380", 12500.00, categoria1),
						new Producto("Apple Watch Series 6", 85000.00, categoria1)).flatMap(producto -> {
							producto.setCreateAt(new Date());
							return productoService.save(producto);
						}))
				.subscribe(producto -> logger.info("INSERT: " + producto.getId() + " " + producto.getNombre()));
	}

}
