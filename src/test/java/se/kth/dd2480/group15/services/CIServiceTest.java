package se.kth.dd2480.group15.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import se.kth.dd2480.group15.api.dto.request.PushRequestDTO;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.infrastructure.persistence.BuildRepository;

/**
 * Unit tests for {@link CIService}
 * 
 * This test class verifies:
 * - Correct queueing of build jobs
 * - Correct execution flow for successful build jobs
 * - Correct behavior when clone, build, or test steps fail
 * - Proper interaction with {@link ProcessRunner} and {@link NotifierService}
 * 
 * All external dependencies are mocked using Mockito.
 */
class CIServiceTest {

    @Mock
    private ProcessRunner processRunner;

    @Mock
    private NotifierService notifierService;

    @Mock
    BuildRepository buildRepository;

    @InjectMocks
    private CIService ciService;

    /**
     * Initializes Mockito mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    /**
     * Verifies that {@link CIService#queueJob(PushRequestDTO)}
     * correctly creates a {@link Build} and adds it to the internal queue.
     *
     * This test ensures:
     * - The queue is initially empty
     * - A job is added after calling {@code queueJob}
     * - The created Build contains the correct commit SHA, repository URL, and owner
     */
    @Test
    void testQueueJobAddsBuildToQueue() {
        PushRequestDTO dto = mock(PushRequestDTO.class);
        PushRequestDTO.Repository repo = mock(PushRequestDTO.Repository.class);
        PushRequestDTO.Owner owner = mock(PushRequestDTO.Owner.class);

        when(dto.after()).thenReturn("commit123");
        when(dto.getRepository()).thenReturn(repo);
        when(repo.clone_url()).thenReturn("url456");
        when(repo.getOwner()).thenReturn(owner);
        when(owner.getName()).thenReturn("owner789");


        // Verify that queue is empty when nothing has been added
        BlockingQueue<Build> queue = getQueue(ciService);
        assertEquals(0, queue.size());

        // Verify that queue contains 1 object when one has been added
        ciService.queueJob(dto);
        assertEquals(1, queue.size());

        // Verify that the correct job is in the queue
        Build job = queue.peek();
        assertNotNull(job);
        assertEquals("commit123", job.getCommitSha());
        assertEquals("url456", job.getRepoUrl());
        assertEquals("owner789", job.getRepoOwner());
    }

    /**
     * Verifies that {@link CIService#handleJob(Build)} behaves correctly for a
     * successful job.
     * 
     * This test ensures:
     * - Clone, build, and test executes in order when all succeed
     * - The build is marked as finished
     * - A success notification is sent
     * - Cleanup is performed
     */
    @Test
    void testHandleJobSuccess() {
        Build job = mock(Build.class);

        when(processRunner.cloneRepo(eq(job), any())).thenReturn(true);
        when(processRunner.build(eq(job), any())).thenReturn(true);
        when(processRunner.test(eq(job), any())).thenReturn(true);
        when(job.getRepoName()).thenReturn("name123");
        when(job.getCommitSha()).thenReturn("commit456");
        when(job.getRepoOwner()).thenReturn("owner789");


        ciService.handleJob(job);

        // Verify processService calls
        verify(processRunner).cloneRepo(eq(job), any());
        verify(processRunner).build(eq(job), any());
        verify(processRunner).test(eq(job), any());
        verify(processRunner).cleanup(job);

        // Verify build finished
        verify(job).finishBuild();
        verify(job, never()).failBuild();

        // Verify notifier called
        verify(notifierService).notify(
            job.getRepoOwner(),
            job.getRepoName(),
            job.getCommitSha(),
            "success",
            "Clone: Success\nBuild: Success\nTest: Success"
        );
    }

    /**
     * Verifies that {@link CIService#handleJob(Build)} behaves correctly when
     * the clone step fails.
     * 
     * This test ensures:
     * - Clone step is executed
     * - Build and test steps are NOT executed
     * - The build is marked as failed
     * - A failure notification is sent
     * - Cleanup is still executed
     */
    @Test
    void testHandleJobCloneFails() {
        Build job = mock(Build.class);

        when(processRunner.cloneRepo(eq(job), any())).thenReturn(false);
        when(job.getRepoName()).thenReturn("name123");
        when(job.getCommitSha()).thenReturn("commit456");
        when(job.getRepoOwner()).thenReturn("owner789");


        ciService.handleJob(job);

        // Verify processService calls
        verify(processRunner).cloneRepo(eq(job), any());
        verify(processRunner, never()).build(eq(job), any());
        verify(processRunner, never()).test(eq(job), any());

        verify(processRunner).cleanup(job);

        // Verify build fails
        verify(job).failBuild();
        verify(job, never()).finishBuild();

        // Verify notifier called
        verify(notifierService).notify(
                job.getRepoOwner(),
                job.getRepoName(),
                job.getCommitSha(),
                "failure",
                "Clone: Fail"
        );
    }

    /**
     * Verifies that {@link CIService#handleJob(Build)} behaves correctly when
     * the build step fails.
     * 
     * This test ensures:
     * - Clone and build steps are executed
     * - Test step is NOT executed
     * - The build is marked as failed
     * - A failure notification is sent
     * - Cleanup is still executed
     */
    @Test
    void testHandleJobBuildFails() {
        Build job = mock(Build.class);

        when(processRunner.cloneRepo(eq(job), any())).thenReturn(true);
        when(processRunner.build(eq(job), any())).thenReturn(false);
        when(job.getRepoName()).thenReturn("name123");
        when(job.getCommitSha()).thenReturn("commit456");
        when(job.getRepoOwner()).thenReturn("owner789");


        ciService.handleJob(job);

        // Verify processService calls
        verify(processRunner).cloneRepo(eq(job), any());
        verify(processRunner).build(eq(job), any());
        verify(processRunner, never()).test(eq(job), any());
        verify(processRunner).cleanup(job);

        // Verify build fails
        verify(job).failBuild();
        verify(job, never()).finishBuild();

        // Verify notifier called
        verify(notifierService).notify(
                job.getRepoOwner(),
                job.getRepoName(),
                job.getCommitSha(),
                "failure",
                "Clone: Success\nBuild: Fail"
        );
    }

    /**
     * Verifies that {@link CIService#handleJob(Build)} behaves correctly when
     * the test step fails.
     * 
     * This test ensures:
     * - Clone and build succeed
     * - The build is marked as failed
     * - A failure notification is sent
     * - Cleanup is executed     
     */
    @Test
    void testHandleJobTestFails() {
        Build job = mock(Build.class);

        when(processRunner.cloneRepo(eq(job), any())).thenReturn(true);
        when(processRunner.build(eq(job), any())).thenReturn(true);
        when(processRunner.test(eq(job), any())).thenReturn(false);
        when(job.getRepoName()).thenReturn("name123");
        when(job.getCommitSha()).thenReturn("commit456");
        when(job.getRepoOwner()).thenReturn("owner789");

        ciService.handleJob(job);


        // Verify processService calls
        verify(processRunner).cloneRepo(eq(job), any());
        verify(processRunner).build(eq(job), any());
        verify(processRunner).test(eq(job), any());
        verify(processRunner).cleanup(job);

        // Verify build fails
        verify(job).failBuild();
        verify(job, never()).finishBuild();

        // Verify notifier called
        verify(notifierService).notify(
                job.getRepoOwner(),
                job.getRepoName(),
                job.getCommitSha(),
                "failure",
                "Clone: Success\nBuild: Success\nTest: Fail"
        );
    }

    /**
     * Helper method used to access the private queue field in {@link CIService}
     * via reflection.
     * 
     * @param service the CIService instance
     * @return the internal BlockingQueue of builds
     */
    @SuppressWarnings("unchecked")
    private BlockingQueue<Build> getQueue(CIService service) {
        try {
            var field = CIService.class.getDeclaredField("queue");
            field.setAccessible(true);
            return (BlockingQueue<Build>) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
