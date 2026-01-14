package com.davidcerdeiro.documind.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import com.davidcerdeiro.documind.service.DocumentService;

@SpringBootTest
public class DocumentUnitTest {
    @Autowired
    private DocumentService documentService;

    @Value("classpath:pdfs/sample-test.pdf") 
    private Resource testPdfResource;

    @Test
    void testChunkingDocument() {
        var chunks = documentService.chunkingDocument(testPdfResource);
        // Basic assertion to ensure chunks were created
        assert(chunks.size() > 0);
        // Verify that no chunk starts or ends with spaces and none are blank
        chunks.forEach(chunk -> {
            String content = chunk.getText();
            assertFalse(content.startsWith(" "), "The chunk should not start with spaces (Trim failed)");
            assertFalse(content.endsWith(" "), "The chunk should not end with spaces (Trim failed)");
            assertFalse(content.isBlank(), "The chunk should not be blank");
        });

        boolean containsContent = chunks.stream()
            .anyMatch(chunk -> chunk.getText().contains("Tristique"));
            
        assertTrue(containsContent, "The chunks should contain specific content from the PDF");
    }
}
