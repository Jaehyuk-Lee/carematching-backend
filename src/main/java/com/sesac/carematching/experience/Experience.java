package com.sesac.carematching.experience;

import com.sesac.carematching.caregiver.Caregiver;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "exercise")
@NoArgsConstructor
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ENO", nullable = false)
    private Integer id;

    @Size(max = 5)
    @Column(name = "LOCATION", nullable = false, length = 5)
    private String location;

    @Size(max = 50)
    @NotNull
    @Column(name = "TITLE", nullable = false, length = 50)
    private String title;

    @Size(max = 100)
    @NotNull
    @Column(name = "SUMMARY", nullable = false)
    private String summary;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CNO", nullable = false)
    private Caregiver caregiver;

    @Builder
    public Experience(String location, String title, String summary) {
        this.location = location;
        this.title = title;
        this.summary = summary;
    }
}
