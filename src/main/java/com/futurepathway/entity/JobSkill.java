package com.futurepathway.entity;

import jakarta.persistence.*;
import lombok.*;

/** Join table: required skill set per job (spec §3 JOB_SKILL). */
@Entity
@Table(name = "job_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSkill {

    @EmbeddedId
    private JobSkillId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("jobId")
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "required_proficiency", nullable = false)
    @Builder.Default
    private Integer requiredProficiency = 3;
}
