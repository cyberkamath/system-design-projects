package sh.kamath.url_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "short_url")
@Setter
@Getter
@NoArgsConstructor
public class ShortUrl implements Persistable<Long> {

    // ID is externally-generated from short_url_id_seq via UrlService.
    // @GeneratedValue is deliberately omitted so persist() accepts pre-set IDs.
    // Persistable.isNew() drives INSERT vs UPDATE routing in save().
    @Id
    private Long id;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "created_date", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    // Track whether this entity has been persisted yet
    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public Long getId() {
        return id;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortUrl that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ShortUrl{" +
                "id=" + id +
                ", originalUrl='" + originalUrl + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}