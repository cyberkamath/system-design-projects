package sh.kamath.analytics_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sh.kamath.analytics_service.entity.ClickEvent;

public interface ClickEventRepository extends JpaRepository<ClickEvent,Long> {

}
