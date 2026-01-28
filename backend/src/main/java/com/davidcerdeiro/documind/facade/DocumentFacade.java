package com.davidcerdeiro.documind.facade;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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


    public String processAndSaveDocumentAsync(MultipartFile file) {
        // 1. Validación rápida (Síncrona)
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new InvalidFileTypeException("The file must be a PDF. Received: " + file.getContentType());
        }

        // 2. Generar ID único para este proceso
        String jobId = UUID.randomUUID().toString();

        // 3. Lanzar el proceso en segundo plano (Fuego y olvido)
        try {
            // Pasamos el recurso del archivo. Spring maneja el recurso en memoria temporalmente.
            documentService.processFileAsync(jobId, file.getResource());
        } catch (Exception e) {
            throw new RuntimeException("Error initiating document processing", e);
        }

        // 4. Retornar el ID inmediatamente para que el usuario no espere
        return jobId;
    }
    
    // Método para consultar el estado
    public String getProcessingStatus(String jobId) {
        return documentService.getStatus(jobId);
    }

    public String promptModel(String question) {
        List<Document> similarDocuments = documentService.similaritySearch(question);

        if (similarDocuments.isEmpty()) {
            throw new NoDocumentsException("The question "+ question + " doesn't have related info in the document");
        }

        String response = documentService.promptModel(similarDocuments, question);

        return response;
    }
}
