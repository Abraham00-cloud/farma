package com.project.farma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FarmaApplication {

	public static void main(String[] args) {
		SpringApplication.run(FarmaApplication.class, args);
	}

}
