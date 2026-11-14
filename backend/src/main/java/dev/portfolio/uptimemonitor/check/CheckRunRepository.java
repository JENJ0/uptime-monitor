package dev.portfolio.uptimemonitor.check;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CheckRunRepository extends JpaRepository<CheckRun, Long> {
    Page<CheckRun> findByMonitorIdOrderByCheckedAtDescIdDesc(Long monitorId, Pageable pageable);
}
