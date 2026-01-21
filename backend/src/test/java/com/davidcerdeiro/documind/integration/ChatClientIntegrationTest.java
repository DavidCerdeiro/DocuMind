package com.davidcerdeiro.documind.integration;

import com.davidcerdeiro.documind.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// This class only will be executed in a local environment.
@TestPropertySource(properties = {
    "spring.ai.ollama.chat.enabled=true",
    "spring.ai.ollama.base-url=http://localhost:11434",
    "spring.ai.ollama.chat.options.model=phi3" 
})
@EnabledIf("isOllamaRunning")
public class ChatClientIntegrationTest extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ChatClientIntegrationTest.class);

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    static boolean isOllamaRunning() {
        try {
            java.net.Socket socket = new java.net.Socket("localhost", 11434);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeEach
    void cleanDatabase() {
        // Clean the vector table before each test
        jdbcTemplate.execute("DELETE FROM vector_store");
    }

    @Test
    void verifyOllamaAndPhi3Response() {
        log.info("Starting connection test with Ollama (Phi3)...");
        try {
            String response = chatClient.prompt()
                    .user("Answer with a single word: Are you alive?")
                    .call()
                    .content();

            log.info("Ollama response received: {}", response);
            assertThat(response).isNotBlank();
        } catch (Exception e) {
            log.error("Error connecting to Ollama.");
            throw e;
        }
    }

    @Test
    @DisplayName("RAG Flow: It would answer correctly to the question")
    void shouldAnswerQuestionFromDocument() {
        // --- ARRANGE ---
        // A example document
        String uniqueContent = "Cristiano Ronaldo is my favourite player";
        Document doc = new Document(uniqueContent, Map.of("source", "favourite_player.pdf"));
        
        // Store in pgvector
        documentService.saveDocument(List.of(doc));

        String question = "¿Who is my favourite player?";
        
        List<Document> similarDocs = documentService.similaritySearch(question);
        assertThat(similarDocs).isNotEmpty();
        
        // --- ACT ---
        String response = documentService.promptModel(similarDocs, question, "en");
        log.info("RAG answer: {}", response);

        // --- ASSERT ---
        assertThat(response).contains("Cristiano Ronaldo");
    }

    @Test
    @DisplayName("Anti-Hallucination: It should say it doesn't know if the information is NOT in the document")
    void shouldAvoidHallucination() {
        // --- ARRANGE ---
        String content = "Java 21 was released in September 2023 and includes new features such as Record Patterns and Pattern Matching for switch.";
        Document doc = new Document(content);
        documentService.saveDocument(List.of(doc));

        String question = "Which is the recipe of paella?";
        
        List<Document> similarDocs = documentService.similaritySearch(question);
        
        // --- ACT ---
        String response = documentService.promptModel(similarDocs, question, "en");
        log.info("Anti-Hallucination Response: {}", response);

        // --- ASSERT ---
        assertThat(response)
            .as("The model should not invent answers out of context")
            .containsIgnoringCase("I do not have that information in the provided documents.");
    }

}