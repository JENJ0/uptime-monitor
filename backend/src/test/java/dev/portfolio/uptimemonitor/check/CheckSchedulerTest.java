package dev.portfolio.uptimemonitor.check;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import dev.portfolio.uptimemonitor.monitor.MonitorRepository;
import dev.portfolio.uptimemonitor.monitor.Monitor;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckSchedulerTest {    
    
    @Test
    void secondTickNoCheckIfAlreadyInFlight() throws InterruptedException {
        MonitorRepository repository = Mockito.mock(MonitorRepository.class);
        MonitorCheckService checkService = Mockito.mock(MonitorCheckService.class);
        Monitor monitor = Mockito.mock(Monitor.class);
        when(monitor.getId()).thenReturn(1L);
        when(repository.findDueMonitors(any())).thenReturn(List.of(monitor));

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(checkService.runCheck(1L)).thenAnswer(invocation -> {
            started.countDown();
            release.await(); // Wait until release is counted down
            return null;
        });

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(2);
        executor.initialize();

        CheckScheduler scheduler = new CheckScheduler(repository, checkService, executor);

        scheduler.tick();
        assertTrue(started.await(2, TimeUnit.SECONDS)); 
        scheduler.tick();
        release.countDown();
        executor.shutdown();

        verify(checkService, times(1)).runCheck(1L);
    }
}
