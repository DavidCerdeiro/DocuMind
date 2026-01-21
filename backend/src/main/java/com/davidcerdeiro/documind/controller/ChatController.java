package com.davidcerdeiro.documind.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.davidcerdeiro.documind.dto.ChatRequest;
import com.davidcerdeiro.documind.dto.ChatResponse;
import com.davidcerdeiro.documind.facade.DocumentFacade;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final DocumentFacade documentFacade;

    public ChatController(DocumentFacade documentFacade){
        this.documentFacade = documentFacade;
    }
    //Endpoint to ask to the model
    // Responses:
    // 200 OK: Question successfully answered
    // 404 Not Found: If the ask doesn't have info related in the document
    @PostMapping
    public ResponseEntity<ChatResponse> getMethodName(@RequestBody ChatRequest request) {
        String answer = documentFacade.promptModel(request.question());

        return ResponseEntity.ok(new ChatResponse(answer));
    }
}
