package fr.enedis.teme.assertapi.server;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

@SpringBootApplication
public class AssertapiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssertapiServerApplication.class, args);
	}

    @Bean
    @Primary
	public static final ObjectMapper defaultObjectMapper() {
		return Jackson2ObjectMapperBuilder.json().build()
			.registerModule(new JavaTimeModule())
			.registerModule(new ParameterNamesModule())
			.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
}
