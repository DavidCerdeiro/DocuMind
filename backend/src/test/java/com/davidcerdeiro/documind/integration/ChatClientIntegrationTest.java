package com.davidcerdeiro.documind.integration;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.condition.EnabledIf;
import static org.assertj.core.api.Assertions.assertThat;

//This class only will be executed in a local enviroment. This is in order to avoid install ollama in github
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

    static boolean isOllamaRunning() {
        try {
            java.net.Socket socket = new java.net.Socket("localhost", 11434);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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