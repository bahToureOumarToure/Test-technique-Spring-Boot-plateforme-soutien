package com.soutien.controller;

import com.soutien.dto.MessageCreateRequest;
import com.soutien.dto.MessageResponse;
import com.soutien.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de la messagerie.
 * Les messages sont imbriqués sous une demande :
 *   /api/requests/{requestId}/messages
 *
 * Les contrôles d'accès (participant / admin) sont dans le service.
 */
@RestController
@RequestMapping("/api/requests/{requestId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * POST /api/requests/{requestId}/messages -> envoyer un message.
     */
    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @PathVariable Long requestId,
            @Valid @RequestBody MessageCreateRequest request) {
        MessageResponse created = messageService.sendMessage(requestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/requests/{requestId}/messages -> historique du fil.
     */
    @GetMapping
    public List<MessageResponse> history(@PathVariable Long requestId) {
        return messageService.getHistory(requestId);
    }
}
