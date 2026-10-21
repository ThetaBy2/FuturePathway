package com.futurepathway.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Login row (name / email / password_hash per spec §3 STUDENT). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    @Column(name = "roll_no", nullable = false, unique = true, length = 40)
    private String rollNo;

    @Column(nullable = false, length = 80)
    private String branch;

    @Column(name = "batch_year", nullable = false)
    private Integer batchYear;

    @Column(precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "backlog_count", nullable = false)
    @Builder.Default
    private Integer backlogCount = 0;

    @Column(name = "cv_url", length = 500)
    private String cvUrl;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    /** Plain-text CV pasted/uploaded by the student — primary Gemini input. */
    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
