package com.administrativetool.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
