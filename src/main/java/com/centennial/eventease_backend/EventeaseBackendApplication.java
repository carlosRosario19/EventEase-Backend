package com.centennial.eventease_backend;

import com.centennial.eventease_backend.security.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication
public class EventeaseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventeaseBackendApplication.class, args);
	}

}
