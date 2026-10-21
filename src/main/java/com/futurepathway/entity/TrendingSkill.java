package com.futurepathway.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "trending_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false, unique = true)
    private Skill skill;

    @Column(name = "demand_score", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal demandScore = BigDecimal.ZERO;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String source = "JOB_POSTINGS";

    @Column(columnDefinition = "TEXT")
    private String insight;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
}
