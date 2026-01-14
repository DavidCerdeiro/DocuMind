package com.davidcerdeiro.documind.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    
    @Value("${app.document.chunk-size}")
    public int chunkSize;

    @Value("${app.document.chunk-overlap}")
    public int chunkOverlap;

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
}
