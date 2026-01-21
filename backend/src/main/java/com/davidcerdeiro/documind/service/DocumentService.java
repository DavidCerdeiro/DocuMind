package com.davidcerdeiro.documind.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    
    @Value("${app.document.chunk-size}")
    public int chunkSize;

    @Value("${app.document.chunk-overlap}")
    public int chunkOverlap;

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    public DocumentService(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    // Method to chunk PDF document
    public List<Document> chunkingDocument(Resource document) {
        // First, read the PDF document
        PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(document);
        List<Document> documents = pdfDocumentReader.read();

        // Clean documents by trimming whitespace
        List<Document> cleanedDocuments = documents.stream()
                .map(doc -> new Document(doc.getText().trim(), doc.getMetadata()))
                .toList();

        // Now, chunk the cleaned documents
        TokenTextSplitter textSplitter = new TokenTextSplitter(chunkSize, chunkOverlap, 5, 10000, true);
        
        return textSplitter.apply(cleanedDocuments);
    }

    // Method to save documents to vector store
    public void saveDocument(List<Document> document) {
        vectorStore.add(document);
    }

    public List<Document> similaritySearch(String question){
        SearchRequest searchRequest = SearchRequest.builder()
            .query(question)
            .topK(5) 
            .similarityThreshold(0.8)
            .build();
        
        return vectorStore.similaritySearch(searchRequest);
    }

    public String promptModel(List<Document> similarDocuments, String question){
        // Build the context
        String context = similarDocuments.stream()
            .map(Document::getText)
            .collect(Collectors.joining(System.lineSeparator()));
        
        return chatClient.prompt()
            .system(s -> s.text("""
                Eres un asistente experto llamado DocuMind.
                Tu tarea es responder preguntas basándote EXCLUSIVAMENTE en el siguiente contexto.
                
                CONTEXTO:
                {context_str}
                
                Si la respuesta no está en el contexto, di: "No dispongo de esa información en los documentos proporcionados".
                """)
                .param("context_str", context))
            .user(question)
            .call()
            .content();

    }
}
