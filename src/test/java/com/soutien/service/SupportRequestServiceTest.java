package com.soutien.service;

import com.soutien.dto.SupportRequestCreateRequest;
import com.soutien.dto.SupportRequestResponse;
import com.soutien.entity.*;
import com.soutien.exception.BusinessException;
import com.soutien.repository.SubjectRepository;
import com.soutien.repository.SupportRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests UNITAIRES de la logique métier des demandes.
 * On ne démarre PAS Spring : on simule (mock) les dépendances
 * pour tester uniquement les règles de SupportRequestService.
 */
@ExtendWith(MockitoExtension.class)
class SupportRequestServiceTest {

    @Mock private SupportRequestRepository requestRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private UserService userService;

    @InjectMocks private SupportRequestService service;

    private User student;
    private User teacher;
    private User admin;
    private Subject subject;

    @BeforeEach
    void setUp() {
        student = User.builder().id(1L).fullName("Eleve").email("e@t.com").role(Role.STUDENT).build();
        teacher = User.builder().id(2L).fullName("Prof").email("p@t.com").role(Role.TEACHER).build();
        admin   = User.builder().id(3L).fullName("Admin").email("a@t.com").role(Role.ADMIN).build();
        subject = Subject.builder().id(10L).name("Maths").build();
    }

    private SupportRequest aRequest(RequestStatus status, User assignedTeacher) {
        return SupportRequest.builder()
                .id(100L)
                .description("aide")
                .status(status)
                .student(student)
                .teacher(assignedTeacher)
                .subject(subject)
                .build();
    }
// aucun (aucune) ensigenant(e) dispo  donc le champ teacher.. = null
    @Test
    void create_demarreAuStatutCREATED_sansEnseignant() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
        when(requestRepository.save(any(SupportRequest.class))).thenAnswer(i -> i.getArgument(0));

        SupportRequestResponse res = service.create(new SupportRequestCreateRequest(10L, "aide"));

        assertThat(res.status()).isEqualTo(RequestStatus.CREATED);
        assertThat(res.studentId()).isEqualTo(1L);
        assertThat(res.teacherId()).isNull();
    }

    @Test
    void assign_surDemandeCREATED_passeEnIN_PROGRESS() {
        SupportRequest req = aRequest(RequestStatus.CREATED, null);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(userService.getCurrentUser()).thenReturn(teacher);

        SupportRequestResponse res = service.assignToCurrentTeacher(100L);

        assertThat(res.status()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(res.teacherId()).isEqualTo(2L);
    }

    @Test
    void assign_surDemandeDejaEnCours_estRejete() {
        SupportRequest req = aRequest(RequestStatus.IN_PROGRESS, teacher);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(userService.getCurrentUser()).thenReturn(teacher);

        assertThatThrownBy(() -> service.assignToCurrentTeacher(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("n'est plus disponible");
    }

    @Test
    void complete_parProprietaire_surDemandeEnCours_passeEnCOMPLETED() {
        SupportRequest req = aRequest(RequestStatus.IN_PROGRESS, teacher);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(userService.getCurrentUser()).thenReturn(student);

        SupportRequestResponse res = service.complete(100L);

        assertThat(res.status()).isEqualTo(RequestStatus.COMPLETED);
    }

    @Test
    void complete_parUnTiers_estInterdit() {
        SupportRequest req = aRequest(RequestStatus.IN_PROGRESS, teacher);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(req));
        // l'enseignant n'est PAS autorisé à clôturer (règle métier)
        when(userService.getCurrentUser()).thenReturn(teacher);

        assertThatThrownBy(() -> service.complete(100L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void complete_surDemandeNonEnCours_estRejete() {
        SupportRequest req = aRequest(RequestStatus.CREATED, null);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(userService.getCurrentUser()).thenReturn(student);

        assertThatThrownBy(() -> service.complete(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EN COURS");
    }

    @Test
    void cancel_parAdmin_surDemandeEnCours_passeEnCANCELLED() {
        SupportRequest req = aRequest(RequestStatus.IN_PROGRESS, teacher);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(userService.getCurrentUser()).thenReturn(admin);

        SupportRequestResponse res = service.cancel(100L);

        assertThat(res.status()).isEqualTo(RequestStatus.CANCELLED);
    }

    @Test
    void cancel_surDemandeDejaTerminee_estRejete() {
        SupportRequest req = aRequest(RequestStatus.COMPLETED, teacher);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(userService.getCurrentUser()).thenReturn(student);

        assertThatThrownBy(() -> service.cancel(100L))
                .isInstanceOf(BusinessException.class);
    }
}
