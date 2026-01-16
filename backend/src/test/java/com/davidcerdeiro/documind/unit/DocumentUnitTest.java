package com.davidcerdeiro.documind.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import com.davidcerdeiro.documind.service.DocumentService;

@ExtendWith(MockitoExtension.class)
public class DocumentUnitTest {
    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "chunkSize", 500); 
        ReflectionTestUtils.setField(documentService, "chunkOverlap", 40); 
    }

    @Test
    void testChunkingDocument() {
        Resource testPdfResource = new ClassPathResource("pdfs/sample-test.pdf");

        if (!testPdfResource.exists()) {
             throw new RuntimeException("sample-test.pdf not found in classpath under pdfs/ directory.");
        }

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
