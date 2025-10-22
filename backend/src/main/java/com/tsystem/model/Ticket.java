package com.tsystem.model;


import com.tsystem.model.enums.*;
import com.tsystem.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotBlank
    @Size(min = 1, max = 160)
    @Column(nullable = false, length = 160)
    private String name; // title

    @Column(columnDefinition = "TEXT")
    @Size(max = 10000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private TicketType type;          // bug | feature | task

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private TicketPriority priority;  // low | med | high

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private TicketState state = TicketState.open; // open | in_progress | done

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();


}
