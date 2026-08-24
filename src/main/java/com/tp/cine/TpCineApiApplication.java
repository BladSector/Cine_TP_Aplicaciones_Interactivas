package com.tp.cine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.tp.cine",
        "modelo",
        "repository",
        "service",
        "controller"
})
@EntityScan(basePackages = "modelo")
@EnableJpaRepositories(basePackages = "repository")
public class TpCineApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TpCineApiApplication.class, args);
    }
}
