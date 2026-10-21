package com.futurepathway.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Persisted Gemini output of {@code generateRoadmap(studentId, targetRole)}. */
@Entity
@Table(name = "roadmap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "target_role", nullable = false, length = 160)
    private String targetRole;

    /** e.g. [{"week":"1-2","title":"...","tasks":[...],"resources":[...]}] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> milestones;

    /** Which engine produced this row: "gemini" or "heuristic-fallback". */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String source = "gemini";

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;
}
