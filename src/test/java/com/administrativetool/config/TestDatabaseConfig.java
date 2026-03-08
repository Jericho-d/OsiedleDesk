package com.administrativetool.config;

import javax.sql.DataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Test configuration that ensures database is cleaned up after test execution.
 * This provides database rollback functionality for integration tests.
 */
@TestConfiguration
public class TestDatabaseConfig {

    /**
     * DataSourceInitializer that runs cleanup scripts after tests.
     * This ensures each test suite starts with a clean database state.
     */
    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        // Clean up script to truncate all tables after tests
        ResourceDatabasePopulator cleanupPopulator = new ResourceDatabasePopulator();
        cleanupPopulator.addScript(new ClassPathResource("db/cleanup.sql"));
        initializer.setDatabasePopulator(cleanupPopulator);

        return initializer;
    }
}
