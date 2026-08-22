package com.tp.cine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.tp.cine",
        "modelo",
        "repository",
        "service",
        "controller"
})
public class TpCineApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TpCineApiApplication.class, args);
    }
}
