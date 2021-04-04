package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class MagicSquareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicSquareApplication.class, args);
    }
}
