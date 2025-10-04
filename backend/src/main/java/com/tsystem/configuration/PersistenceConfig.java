package com.tsystem.configuration;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.tsystem.model")
@EnableJpaRepositories(basePackages = "com.tsystem.repository")
public class PersistenceConfig {

}