package com.davidcerdeiro.documind.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
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
        TikaDocumentReader reader = new TikaDocumentReader(document);
        List<Document> documents = reader.read();
        System.out.println("PDF read. Pages found: " + documents.size());

        // 2. Cleaning (Corrected for Spanish and Chunking)
        List<Document> cleanDocuments = documents.stream().map(doc -> {
        String cleanText = doc.getText()
                    // Normalizamos saltos de línea extraños pero mantenemos separación de párrafos
                    .replaceAll("\\r\\n", "\n") 
                    .replaceAll("[ \\t]+", " ") // Espacios múltiples a uno solo
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
        } finally {
            if (file instanceof FileSystemResource) {
                File diskFile = ((FileSystemResource) file).getFile();
                if (diskFile.exists()) {
                    boolean deleted = diskFile.delete();
                    if (!deleted) {
                        System.err.println("WARN: Could not delete temp file " + diskFile.getAbsolutePath());
                    } else {
                        System.out.println("Temp file cleaned up for job " + fileId);
                    }
                }
            }
        }
    }

    public String getStatus(String fileId) {
        return processStatus.getOrDefault(fileId, "NOT_FOUND");
    }

    // Method to save documents to vector store
    public void saveDocument(List<Document> documents) {
        int batchSize = 1; 
        int total = documents.size();

        System.out.println("Starting embedding generation for " + total + " chunks...");

        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<Document> batch = documents.subList(i, end);
            
            System.out.println("Processing batch " + ((i / batchSize) + 1) + " (" + (i + 1) + " to " + end + " of " + total + ")...");
            
            int totalChars = batch.stream().mapToInt(d -> d.getText().length()).sum();
System.out.println("Enviando batch " + i + " con aprox " + (totalChars / 4) + " tokens.");
            vectorStore.add(batch);
        }
        
        System.out.println("All embeddings saved successfully!");
    }

    public List<Document> similaritySearch(String question) {
        System.out.println("Buscando similitudes para: " + question);
        
        SearchRequest searchRequest = SearchRequest.builder()
            .query(question)
            .topK(8) 
            .similarityThreshold(0.45)
            .build();

        List<Document> docs = vectorStore.similaritySearch(searchRequest);
        
        System.out.println("--- CHUNKS ENCONTRADOS (" + docs.size() + ") ---");
        docs.forEach(d -> {
            System.out.println("Score: " + d.getMetadata().get("distance")); // O score según impl
            String preview = d.getText().length() > 100 ? d.getText().substring(0, 100) : d.getText();
            System.out.println("Contenido: " + preview.replace("\n", " ") + "...");
        });
        System.out.println("----------------------------------------------");

        return docs;
    }

    public String promptModel(List<Document> similarDocuments, String question) {
        // Building context from similar documents, separated by tags
        String context = similarDocuments.stream()
                .map(doc -> "<fragment>" + System.lineSeparator() + doc.getText() + System.lineSeparator() + "</fragment>")
                .collect(Collectors.joining(System.lineSeparator()));

        // System prompt with rules for the AI model
        String systemText = """
            You are a backend data extraction engine. You are NOT a chat assistant.
            You have no internal knowledge. You can ONLY read the provided XML context.
            
            CONTEXT START:
            <context>
            {context_str}
            </context>
            CONTEXT END.
                        
            INSTRUCTIONS (MUST FOLLOW STRICTLY):
            1.  SEARCH the context for the answer to the user question.
            2.  IF the answer is found: Output ONLY the answer. Max 50 words. No intro. No "Here is the answer".
            3.  IF the answer is NOT strictly in the context: Output EXACTLY: [[NO_INFO_FOUND]]
            4.  NEVER invent data. NEVER give hypothetical examples. NEVER ask follow-up questions.
            5.  LANGUAGE: Answer in the same language as the USER QUESTION.
            """;
        // Prompting the chat model
        String response = chatClient.prompt()
                .system(s -> s.text(systemText).param("context_str", context))
                .user(question)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            return null;
        }

        
        String cleanResponse = response.trim();
        
        if (cleanResponse.contains("[[NO_INFO_FOUND]]")) {
            return null;
        }

        return cleanResponse;
    }

    @Transactional 
    public void clearVectorStore() {
        jdbcTemplate.execute("TRUNCATE TABLE vector_store");
            
        // Clear the in-memory status map
        processStatus.clear();
            
        System.out.println(">>> SUCCESS: Vector database truncated.");
    }
}
