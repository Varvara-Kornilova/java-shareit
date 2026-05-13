package ru.practicum.shareit.config;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTestersAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@Configuration
@Import({JacksonAutoConfiguration.class, JsonTestersAutoConfiguration.class})
@ContextConfiguration
public class EmptyJsonConfig {
}