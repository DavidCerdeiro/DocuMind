package com.davidcerdeiro.documind.integration;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "spring.ai.ollama.chat.enabled=true",
    "spring.ai.ollama.base-url=http://localhost:11434",
    "spring.ai.ollama.chat.options.model=phi3"
})
public class ChatClientIntegrationTest extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ChatClientIntegrationTest.class);

    @Autowired
    private ChatClient chatClient;

    @Test
    void verifyOllamaAndPhi3Response() {
        log.info("Iniciando prueba de conexión con Ollama (Phi3)...");

        try {
            String response = chatClient.prompt()
                    .user("Responde brevemente: ¿Estás funcionando?")
                    .call()
                    .content();

            log.info("Respuesta de Ollama recibida: {}", response);
            assertThat(response).isNotBlank();
            
        } catch (Exception e) {
            log.error("Error de conexión con Ollama. Asegúrate de que 'ollama serve' esté activo y el modelo phi3 descargado.");
            throw e;
        }
    }
}