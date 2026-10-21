package com.futurepathway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FuturePathwayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FuturePathwayApplication.class, args);
    }
}
