package com.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Portfolio API — uygulama giriş noktası.
 *
 * @EnableJpaAuditing: Entity'lerdeki @CreatedDate / @LastModifiedDate
 * alanlarını Spring Data'nın otomatik doldurması için aktif edildi.
 */
@SpringBootApplication
@EnableJpaAuditing
public class PortfolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }
}