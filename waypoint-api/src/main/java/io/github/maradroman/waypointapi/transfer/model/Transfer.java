package io.github.maradroman.waypointapi.transfer.model;

import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    @Builder.Default
    private String type = "allocate";

    @Column(nullable = false)
    @Builder.Default
    private String comment = "";

    @Column(nullable = false)
    private Instant timestamp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
