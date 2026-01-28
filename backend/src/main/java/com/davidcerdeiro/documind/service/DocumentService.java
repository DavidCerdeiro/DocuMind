package com.davidcerdeiro.documind.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    
    @Value("${app.document.chunk-size}")
    public int chunkSize;

    @Value("${app.document.chunk-overlap}")
    public int chunkOverlap;

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    private final Map<String, String> processStatus = new ConcurrentHashMap<>();

    public DocumentService(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    // Method to chunk PDF document
    public List<Document> chunkingDocument(Resource document) {
        // 1. Log de inicio
        System.out.println("Iniciando lectura del PDF...");
        PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(document);
        List<Document> documents = pdfDocumentReader.read();
        System.out.println("PDF leído. Páginas encontradas: " + documents.size());

        // 2. Limpieza (Corregida para Español y Chunking)
        List<Document> cleanDocuments = documents.stream().map(doc -> {
            String cleanText = doc.getText()
                // Elimina caracteres de control raros (pero respeta tildes y ñ)
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "") 
                // Normaliza espacios dobles a uno, pero deja los saltos de línea (\n)
                .replaceAll("[ \\t]+", " ")
                .trim();
            return new Document(cleanText, doc.getMetadata());
        }).toList();

        System.out.println("Limpieza completada. Iniciando división (Chunking)...");

        // 3. Chunking
        // Mantenemos tu config, es buena.
        TokenTextSplitter textSplitter = new TokenTextSplitter(chunkSize, chunkOverlap, 5, 10000, true);
        List<Document> chunks = textSplitter.apply(cleanDocuments);
        
        System.out.println("Chunking finalizado. Total de fragmentos generados: " + chunks.size());
        
        return chunks;
    }

    @Async 
    public void processFileAsync(String fileId, Resource file) {
        try {
            processStatus.put(fileId, "PROCESSING");
            
            // Llamamos a tu lógica lenta existente
            List<Document> chunks = this.chunkingDocument(file);
            this.saveDocument(chunks);
            
            processStatus.put(fileId, "COMPLETED");
            System.out.println("Proceso " + fileId + " terminado.");
            
        } catch (Exception e) {
            processStatus.put(fileId, "ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getStatus(String fileId) {
        return processStatus.getOrDefault(fileId, "NOT_FOUND");
    }

    // Method to save documents to vector store
    public void saveDocument(List<Document> documents) {
        // Lote pequeño para no saturar Ollama corriendo en CPU
        int batchSize = 50; 
        int total = documents.size();

        System.out.println("Iniciando generación de embeddings para " + total + " fragmentos...");

        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<Document> batch = documents.subList(i, end);
            
            System.out.println("Procesando lote " + ((i / batchSize) + 1) + " (" + (i + 1) + " al " + end + " de " + total + ")...");
            
            // Guardamos solo este trozo
            vectorStore.add(batch);
        }
        
        System.out.println("¡Todos los embeddings guardados correctamente!");
    }

    public List<Document> similaritySearch(String question){
        SearchRequest searchRequest = SearchRequest.builder()
            .query(question)
            .topK(3) 
            .similarityThreshold(0.6)
            .build();
        
        return vectorStore.similaritySearch(searchRequest);
    }

    public String promptModel(List<Document> similarDocuments, String question) {
        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        // 1. CAMBIO DE PROMPT: Instrucciones al final y más agresivas
        String systemText = """
            [CONTEXTO]
            {context_str}
            
            [INSTRUCCIONES]
            Eres una IA estricta. Tu trabajo es responder a la pregunta del usuario basándote SOLAMENTE en el [CONTEXTO] de arriba.
            
            REGLAS DE ORO:
            1. Si la respuesta NO está explícitamente en el texto, DEBES responder únicamente con: [[NO_INFO_FOUND]]
            2. NO inventes nada.
            3. SE CONCISO: Responde en menos de 50 palabras si es posible. Ve al grano.
            """;

        String response = chatClient.prompt()
                .system(s -> s.text(systemText).param("context_str", context))
                .user(question)
                .call()
                .content();

        // --- CAPA DE SEGURIDAD (Post-Procesamiento) ---

        // 2. Limpieza básica
        if (response == null || response.isBlank()) {
            return null; // Esto lanzará el 404 en el controller
        }

        // 3. Detección de fallos del modelo (Inglés o alucinaciones de rechazo)
        // Phi-3 a veces dice "I cannot answer", "Sorry", "I don't have info"
        String normalizedResponse = response.toLowerCase();
        
        boolean isRefusal = response.contains("[[NO_INFO_FOUND]]") || 
                            normalizedResponse.contains("sorry, i couldn't") ||
                            normalizedResponse.contains("i cannot answer") ||
                            normalizedResponse.contains("does not contain information") ||
                            normalizedResponse.contains("provided documents do not");

        if (isRefusal) {
            return null; // Forzamos el 404 para que el Frontend muestre el mensaje traducido
        }

        return response;
    }
}
