package com.futurepathway.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Persisted Gemini output of {@code resumeSkillExtraction(studentId)}. */
@Entity
@Table(name = "skill_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /** e.g. [{"skill":"Java","level":4,"evidence":"Built ..."}] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strong_skills", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> strongSkills;

    /** e.g. [{"skill":"Docker","gap":"...","suggestion":"..."}] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weak_skills", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> weakSkills;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    /** Which engine produced this row: "gemini" or "heuristic-fallback". */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String source = "gemini";

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;
}
