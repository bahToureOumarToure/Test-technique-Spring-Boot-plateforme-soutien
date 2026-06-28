package com.soutien;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application.
 *
 * L'annotation @SpringBootApplication active 3 choses :
 *  - @Configuration       : cette classe peut déclarer des beans
 *  - @EnableAutoConfiguration : Spring configure tout seul Tomcat, JPA, etc.
 *  - @ComponentScan       : Spring scanne le package com.soutien pour trouver
 *                           nos @Controller, @Service, @Repository...
 */
@SpringBootApplication
public class PlateformeSoutienApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlateformeSoutienApplication.class, args);
    }
}
