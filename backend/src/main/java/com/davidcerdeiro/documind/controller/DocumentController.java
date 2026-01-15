package com.davidcerdeiro.documind.controller;

import org.springframework.http.ResponseEntity;
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
    @PostMapping
    public ResponseEntity<Void> uploadDocument(@RequestParam("file") MultipartFile file) {
        documentFacade.processAndSaveDocument(file);
        return ResponseEntity.ok().build();
    }
}
