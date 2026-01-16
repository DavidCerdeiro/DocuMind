package com.davidcerdeiro.documind.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Import(BaseIntegrationTest.TestConfig.class)
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> pgvector = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres")
    );

    static {
        pgvector.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pgvector::getJdbcUrl);
        registry.add("spring.datasource.username", pgvector::getUsername);
        registry.add("spring.datasource.password", pgvector::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.ai.ollama.embedding.enabled", () -> "false");
        registry.add("spring.ai.ollama.chat.enabled", () -> "false");
    }

    // Global test configuration for embedding model
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public EmbeddingModel embeddingModel() {
            return new TransformersEmbeddingModel();
        }
    }
}