package com.soutien;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'INTÉGRATION : démarre toute l'application avec la base H2
 * (profil de test) et teste les endpoints HTTP de bout en bout,
 * en passant par la vraie sécurité JWT.
 *
 * Scénario complet : inscription -> création matière -> demande ->
 * affectation -> messagerie -> historique, + un cas non authentifié.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    /** Inscrit un utilisateur et renvoie son token JWT. */
    private String register(String name, String email, String role) throws Exception {
        String body = om.writeValueAsString(Map.of(
                "fullName", name, "email", email, "password", "secret123", "role", role));
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = om.readTree(res.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String bearer(String token) { return "Bearer " + token; }

    @Test
    void parcoursComplet_demandeEtMessagerie() throws Exception {
        // 1. Inscriptions
        String adminTok   = register("Admin",   "it-admin@test.com",   "ADMIN");
        String studentTok = register("Eleve",   "it-eleve@test.com",   "STUDENT");
        String teacherTok = register("Prof",    "it-prof@test.com",    "TEACHER");

        // 2. L'admin crée une matière
        MvcResult subjRes = mockMvc.perform(post("/api/subjects")
                        .header("Authorization", bearer(adminTok))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", "Maths", "description", "Algebre"))))
                .andExpect(status().isCreated())
                .andReturn();
        long subjectId = om.readTree(subjRes.getResponse().getContentAsString()).get("id").asLong();

        // 3. L'élève crée une demande -> CREATED
        MvcResult reqRes = mockMvc.perform(post("/api/requests")
                        .header("Authorization", bearer(studentTok))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("subjectId", subjectId, "description", "Besoin aide"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn();
        long requestId = om.readTree(reqRes.getResponse().getContentAsString()).get("id").asLong();

        // 4. Le prof voit la demande disponible
        mockMvc.perform(get("/api/requests/available")
                        .header("Authorization", bearer(teacherTok)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // 5. Le prof s'affecte -> IN_PROGRESS
        mockMvc.perform(post("/api/requests/" + requestId + "/assign")
                        .header("Authorization", bearer(teacherTok)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.teacherName").value("Prof"));

        // 6. L'élève envoie un message
        mockMvc.perform(post("/api/requests/" + requestId + "/messages")
                        .header("Authorization", bearer(studentTok))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("content", "Bonjour, je bloque"))))
                .andExpect(status().isCreated());

        // 7. Le prof répond
        mockMvc.perform(post("/api/requests/" + requestId + "/messages")
                        .header("Authorization", bearer(teacherTok))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("content", "Bonjour, un exemple ?"))))
                .andExpect(status().isCreated());

        // 8. Historique : 2 messages, le 1er de l'élève
        mockMvc.perform(get("/api/requests/" + requestId + "/messages")
                        .header("Authorization", bearer(studentTok)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].senderRole").value("STUDENT"))
                .andExpect(jsonPath("$[1].senderRole").value("TEACHER"));

        // 9. L'élève termine sa demande -> COMPLETED
        mockMvc.perform(patch("/api/requests/" + requestId + "/complete")
                        .header("Authorization", bearer(studentTok)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void accesSansToken_estRefuse401() throws Exception {
        mockMvc.perform(get("/api/requests/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eleveNePeutPasCreerUneMatiere_403() throws Exception {
        String studentTok = register("Eleve2", "it-eleve2@test.com", "STUDENT");
        mockMvc.perform(post("/api/subjects")
                        .header("Authorization", bearer(studentTok))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", "Triche", "description", "x"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void contexteSeCharge() {
        assertThat(mockMvc).isNotNull();
    }
}
