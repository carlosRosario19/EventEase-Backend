package com.centennial.eventease_backend;

import com.centennial.eventease_backend.security.RsaKeyProperties;
import com.centennial.eventease_backend.security.StorageProperties;
import com.centennial.eventease_backend.services.contracts.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties({RsaKeyProperties.class, StorageProperties.class})
@SpringBootApplication
public class EventeaseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventeaseBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			//storageService.deleteAll(); unable this line to remove all the images at the start-up
			storageService.init();
		};
	}

}
