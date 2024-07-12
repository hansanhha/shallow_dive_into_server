package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@SpringBootApplication
public class CloudNativeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudNativeApplication.class, args);
	}

}

@Entity
class Cat {
	@Id
	@GeneratedValue
	private Long id;
	private String name;

	public Cat() {
	}

	public Cat(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Cat{" + "id=" + id + ", name=" + name + '\'' + '}';
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}

@RepositoryRestResource
interface CatRepository extends JpaRepository<Cat, Long> {

}