package com.davidcerdeiro.documind.facade;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import com.davidcerdeiro.documind.exception.InvalidFileTypeException;
import com.davidcerdeiro.documind.exception.NoDocumentsException;
import com.davidcerdeiro.documind.service.DocumentService;

@Component
public class DocumentFacade {
    
    private final DocumentService documentService;

    public DocumentFacade(DocumentService documentService) {
        this.documentService = documentService;
    }


    public void processAndSaveDocument(MultipartFile file) {
        // If the file is not a PDF, throw an exception
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new InvalidFileTypeException("The file must be a PDF. Received: " + file.getContentType());
        }

        // Process and save the document
        try {
            var resource = file.getResource();
            var chunkedDocuments = documentService.chunkingDocument(resource);
            documentService.saveDocument(chunkedDocuments);
            
        } catch (Exception e) {
            // Catch any exception and rethrow as a runtime exception
            throw new RuntimeException("Error processing and saving the document", e);
        }
    }

    public String promptModel(String question, String languageCode) {
        List<Document> similarDocuments = documentService.similaritySearch(question);

        if (similarDocuments.isEmpty()) {
            throw new NoDocumentsException("The question "+ question + " doesn't have related info in the document");
        }

        String response = documentService.promptModel(similarDocuments, question, languageCode);

        return response;
    }
}
