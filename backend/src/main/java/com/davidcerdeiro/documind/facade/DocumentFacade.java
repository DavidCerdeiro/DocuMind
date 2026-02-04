package com.davidcerdeiro.documind.facade;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import org.springframework.core.io.FileSystemResource;
import com.davidcerdeiro.documind.dto.JobStatus;
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
        // 1. Validate file type
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new InvalidFileTypeException("The file must be a PDF. Received: " + file.getContentType());
        }

        // 2. Generate unique ID for this process
        String jobId = UUID.randomUUID().toString();

        // 3. Launch the process in the background
        try {
            Path tempFile = Files.createTempFile("documind_" + jobId, ".pdf");
            file.transferTo(tempFile.toFile());

            // 4. Launch async process with the FileSystemResource (Persistent)
            documentService.processFileAsync(jobId, new FileSystemResource(tempFile.toFile()));
        } catch (Exception e) {
            throw new RuntimeException("Error initiating document processing", e);
        }

        // 4. Return the ID immediately so the user doesn't have to wait
        return jobId;
    }
    
    // MMethod to check the status
    public JobStatus getProcessingStatus(String jobId) {
        JobStatus status = documentService.getStatus(jobId);
        return status;
    }

    public String promptModel(String question) {
        List<Document> similarDocuments = documentService.similaritySearch(question);

        if (similarDocuments.isEmpty()) {
            throw new NoDocumentsException("The question "+ question + " doesn't have related info in the document");
        }

        String response = documentService.promptModel(similarDocuments, question);

        return response;
    }

    public void clearVectorStore() {
        documentService.clearVectorStore();
    }

}
