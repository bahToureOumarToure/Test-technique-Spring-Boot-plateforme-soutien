package com.soutien.controller;

import com.soutien.dto.UserResponse;
import com.soutien.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller des utilisateurs.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users -> liste tous les utilisateurs.
     * @PreAuthorize : SEUL un ADMIN peut appeler cette route.
     * (vérifié AVANT d'entrer dans la méthode ; sinon -> 403 Forbidden)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    /**
     * GET /api/users/{id} -> détail d'un utilisateur (ADMIN).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}
