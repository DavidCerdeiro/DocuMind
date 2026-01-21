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
            .similarityThreshold(0.6)
            .build();
        
        return vectorStore.similaritySearch(searchRequest);
    }

    public String promptModel(List<Document> similarDocuments, String question, String languageCode) {
        String lang = (languageCode == null || languageCode.isBlank()) ? "es" : languageCode.toLowerCase();
        
        String refusalPhrase = switch (lang) {
            case "en" -> "I do not have that information in the provided documents.";
            default -> "No dispongo de esa información en los documentos proporcionados.";
        };

        String langInstruction = switch (lang) {
            case "en" -> "Answer exclusively in English.";
            default -> "Responde exclusivamente en Español.";
        };

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String rawResponse = chatClient.prompt()
            .system(s -> s.text("""
                Eres DocuMind, un asistente de análisis documental robótico y estricto.
                
                INSTRUCCIONES DE IDIOMA:
                {lang_instruction}

                --- COMIENZO DEL CONTEXTO ---
                {context_str}
                --- FIN DEL CONTEXTO ---

                REGLAS ABSOLUTAS (LEER ATENTAMENTE):
                1. Tu ÚNICA fuente de verdad es el texto entre las marcas de CONTEXTO anteriores.
                2. Si la respuesta a la pregunta del usuario no se encuentra explícitamente en el CONTEXTO, debes responder ÚNICAMENTE con la siguiente frase exacta:
                "{refusal_phrase}"
                3. NO uses conocimientos previos. NO inventes. NO des recetas, código o datos que no estén arriba.
                4. Si el contexto está vacío o no es relevante, usa la frase de rechazo.
                """)
            .param("context_str", context)
            .param("lang_instruction", langInstruction)
            .param("refusal_phrase", refusalPhrase))
            .user(question)
            .call()
            .content();
        
        if (rawResponse.contains(refusalPhrase)) {
            return refusalPhrase;
        }

        return rawResponse;
    }
}
