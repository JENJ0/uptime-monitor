package dev.portfolio.uptimemonitor.monitor;
import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    @Query(value = "SELECT m.* FROM monitors m "
            + "WHERE NOT EXISTS ("
            + "SELECT 1 FROM check_results c "
            + "WHERE c.monitor_id = m.id "
            + "AND c.checked_at > CAST(:now AS timestamptz) - m.interval_seconds * INTERVAL '1 second'"
            + ")", nativeQuery = true)
    List<Monitor> findDueMonitors(@Param("now") Instant now);
}
        