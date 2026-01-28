package com.davidcerdeiro.documind.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.davidcerdeiro.documind.facade.DocumentFacade;

@RestController
@RequestMapping("/api/docs")
public class DocumentController {
    
    private final DocumentFacade documentFacade;

    public DocumentController(DocumentFacade documentFacade) {
        this.documentFacade = documentFacade;
    }
    
    // Endpoint to upload document
    // Responses:
    // 200 OK: Document processed and saved successfully
    // 415 Unsupported Media Type: If the uploaded file is not a PDF
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) {
        String jobId = documentFacade.processAndSaveDocumentAsync(file);
        
        return ResponseEntity.accepted().body(Map.of(
            "jobId", jobId,
            "status", "PROCESSING"
        ));
    }

    // Endpoint to check processing status
    // Responses:
    // 200 OK: Returns the current status of the document processing
    // 404 Not Found: If the provided jobId does not exist
    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable String jobId) {
        String status = documentFacade.getProcessingStatus(jobId);
        
        if ("NOT_FOUND".equals(status)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
            "jobId", jobId,
            "status", status
        ));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearVectorStore() {
        documentFacade.clearVectorStore();
        return ResponseEntity.noContent().build();
    }
}
