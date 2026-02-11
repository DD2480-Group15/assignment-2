package se.kth.dd2480.group15.services;

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

class CIServiceTest {

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
     * Helper method used to access the private queue field in {@link XXCIService}
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
