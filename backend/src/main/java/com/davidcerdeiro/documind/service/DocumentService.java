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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {
    
    @Value("${app.document.chunk-size}")
    public int chunkSize;

    @Value("${app.document.chunk-overlap}")
    public int chunkOverlap;

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    private final Map<String, String> processStatus = new ConcurrentHashMap<>();

    private final JdbcTemplate jdbcTemplate;

    public DocumentService(VectorStore vectorStore, ChatClient chatClient, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    // Method to chunk PDF document
    public List<Document> chunkingDocument(Resource document) {
        // 1. Initial logging
        System.out.println("Starting PDF reading...");
        PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(document);
        List<Document> documents = pdfDocumentReader.read();
        System.out.println("PDF read. Pages found: " + documents.size());

        // 2. Cleaning (Corrected for Spanish and Chunking)
        List<Document> cleanDocuments = documents.stream().map(doc -> {
            String cleanText = doc.getText()
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "") 
                .replaceAll("[ \\t]+", " ")
                .trim();
            return new Document(cleanText, doc.getMetadata());
        }).toList();

        System.out.println("Cleaning completed. Starting chunking...");

        // 3. Chunking
        TokenTextSplitter textSplitter = new TokenTextSplitter(chunkSize, chunkOverlap, 5, 10000, true);
        List<Document> chunks = textSplitter.apply(cleanDocuments);
        
        System.out.println("Chunking completed. Total chunks generated: " + chunks.size());
        
        return chunks;
    }

    @Async 
    public void processFileAsync(String fileId, Resource file) {
        try {
            processStatus.put(fileId, "PROCESSING");
            
            List<Document> chunks = this.chunkingDocument(file);
            this.saveDocument(chunks);
            
            processStatus.put(fileId, "COMPLETED");
            System.out.println("Process " + fileId + " completed.");
            
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
        int batchSize = 50; 
        int total = documents.size();

        System.out.println("Starting embedding generation for " + total + " chunks...");

        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<Document> batch = documents.subList(i, end);
            
            System.out.println("Processing batch " + ((i / batchSize) + 1) + " (" + (i + 1) + " to " + end + " of " + total + ")...");
            
            vectorStore.add(batch);
        }
        
        System.out.println("All embeddings saved successfully!");
    }

    public List<Document> similaritySearch(String question) {
        System.out.println("Buscando similitudes para: " + question);
        
        SearchRequest searchRequest = SearchRequest.builder()
            .query(question)
            .topK(6) 
            .similarityThreshold(0.4)
            .build();

        List<Document> docs = vectorStore.similaritySearch(searchRequest);
        
        // DEBUG: Ver qué demonios está encontrando
        System.out.println("--- CHUNKS ENCONTRADOS (" + docs.size() + ") ---");
        docs.forEach(d -> {
            System.out.println("Score: " + d.getMetadata().get("distance")); // O score según impl
            // Imprimimos solo los primeros 100 caracteres para no ensuciar
            String preview = d.getText().length() > 100 ? d.getText().substring(0, 100) : d.getText();
            System.out.println("Contenido: " + preview.replace("\n", " ") + "...");
        });
        System.out.println("----------------------------------------------");

        return docs;
    }

    public String promptModel(List<Document> similarDocuments, String question) {
        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        // OPTIMIZACIÓN DEL PROMPT:
        // 1. Reglas PRIMERO (evita fuga al final).
        // 2. Inglés técnico para mejor obediencia del modelo (Phi-3).
        // 3. Instrucción explícita de idioma de salida dinámico.
        String systemText = """
            You are a precise and helpful assistant.
            
            RULES:
            1. LANGUAGE: Detect the language of the user's question and answer EXCLUSIVELY in that language.
            2. CONCISENESS: Limit your answer to maximum 80 words (approx. 3 sentences). Be direct.
            3. SOURCE: Use ONLY the provided context below. Do not use outside knowledge.
            4. MISSING INFO: If the answer is not in the context, return exactly: [[NO_INFO_FOUND]]
            
            CONTEXT:
            ---------------------
            {context_str}
            ---------------------
            """;

        String response = chatClient.prompt()
                .system(s -> s.text(systemText).param("context_str", context))
                .user(question)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            return null;
        }

        String normalizedResponse = response.toLowerCase();

        boolean isRefusal = response.contains("[[NO_INFO_FOUND]]") || 
                            normalizedResponse.contains("sorry, i couldn't") ||
                            normalizedResponse.contains("i cannot answer") ||
                            normalizedResponse.contains("does not contain information");

        if (isRefusal) {
            return null;
        }

        return response;
    }

    @Transactional 
    public void clearVectorStore() {
        jdbcTemplate.execute("TRUNCATE TABLE vector_store");
            
        // Clear the in-memory status map
        processStatus.clear();
            
        System.out.println(">>> SUCCESS: Vector database truncated.");
    }
}
