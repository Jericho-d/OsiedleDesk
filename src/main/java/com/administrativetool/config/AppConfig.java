package com.administrativetool.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Value("${spring.liquibase.change-log}")
    private String changeLogPath;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        final var liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changeLogPath);
        liquibase.setDataSource(dataSource);
        return liquibase;
    }
}
