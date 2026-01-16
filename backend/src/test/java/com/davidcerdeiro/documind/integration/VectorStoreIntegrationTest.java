package com.davidcerdeiro.documind.integration;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@Testcontainers
public class VectorStoreIntegrationTest extends BaseIntegrationTest {
    @Autowired
    VectorStore vectorStore;

    // Following the arrange-act-assert pattern, we test ingestion and similarity search
    @Test
    void testIngestAndSimilaritySearch() {
        // --- ARRANGE ---
        // Create sample documents
        Document docA = new Document(
            "Spring Boot make it easy to create stand-alone, production-grade Spring based Applications.", 
            Map.of("meta", "java")
        );
        Document docB = new Document(
            "Photosynthesis is the process by which plants convert light into energy.", 
            Map.of("meta", "biology")
        );

        // --- ACT ---
        // 1. Ingest: Convert text to vector and store in Postgres (Testcontainer)
        vectorStore.add(List.of(docA, docB));

        // 2. Semantic Search: Search by concept, not exact word.
        // The query does not contain the words "Spring" or "Boot", but semantically it is close to docA.
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("modern frameworks for backend development").build()
        );

        // --- ASSERT ---
        Assertions.assertFalse(results.isEmpty(), "The search should return results.");
        
        String contentFound = results.get(0).getText();
        Assertions.assertTrue(contentFound.contains("Spring Boot"), 
            "Semantic search failed. Expected the Spring Boot doc but got: " + contentFound);
            
        System.out.println("Test Passed: Successfully retrieved the document through vector similarity.");
    }
}
