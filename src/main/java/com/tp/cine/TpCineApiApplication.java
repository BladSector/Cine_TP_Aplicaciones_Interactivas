package com.tp.cine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.tp.cine",
        "modelo",
        "repository",
        "service",
        "controller"
})
@EntityScan(basePackages = "modelo")
@EnableJpaRepositories(basePackages = "repository")
@EnableScheduling
public class TpCineApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TpCineApiApplication.class, args);
    }
}
