package sh.kamath.url_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sh.kamath.url_service.entity.ShortUrl;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByCode(String code);

    @Query(value = "SELECT nextval('short_url_id_seq')", nativeQuery = true)
    Long fetchNextId();
}
