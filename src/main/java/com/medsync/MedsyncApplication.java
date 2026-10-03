package com.medsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// @SpringBootApplication is a shortcut for three annotations:
//   @Configuration       → this class can define Spring beans
//   @EnableAutoConfiguration → let Spring auto-configure based on classpath
//   @ComponentScan       → scan com.medsync.* for @Component, @Service, etc.

@SpringBootApplication
@EnableAsync   // activates @Async on methods — needed for our email service
public class MedsyncApplication {

    public static void main(String[] args) {
        // Bootstraps the entire Spring context, starts embedded Tomcat,
        // connects to the database, and begins accepting HTTP requests.
        SpringApplication.run(MedsyncApplication.class, args);
    }
}