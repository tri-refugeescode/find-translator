package org.madridforrefugees.portfolio.find_translator_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FindTranslatorBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindTranslatorBackendApplication.class, args);
    }

}
