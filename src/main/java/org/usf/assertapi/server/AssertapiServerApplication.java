package org.usf.assertapi.server;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.usf.assertapi.core.Utils.defaultMapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootApplication
public class AssertapiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssertapiServerApplication.class, args);
	}

    @Bean
	@Primary
	public static final ObjectMapper defaultObjectMapper() {
		return defaultMapper()
			.registerModule(new JavaTimeModule()) //not sure !
			.configure(FAIL_ON_UNKNOWN_PROPERTIES, false); //strict ??
	}
}
