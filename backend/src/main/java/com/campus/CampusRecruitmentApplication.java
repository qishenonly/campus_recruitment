package com.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.campus.model")
@EnableJpaRepositories("com.campus.repository")
@ComponentScan(basePackages = {"com.campus"})
public class CampusRecruitmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusRecruitmentApplication.class, args);
    }
} 