package se.kth.dd2480.group15.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import se.kth.dd2480.group15.api.dto.request.PushRequestDTO;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.infrastructure.persistence.BuildRepository;

@Service
public class CIService {

    /** Queue holding incoming build jobs waiting to be processed. */
    private BlockingQueue<Build> queue = new LinkedBlockingQueue<>();

    /** Background worker thread responsible for handling queued builds. */
    private Thread thread;


    /** Repository used for persisting build logs and metadata. */
    private final BuildRepository buildRepository;

    public CIService(BuildRepository buildRepository) {
        this.buildRepository = buildRepository;
    }

    /**
     * Converts an incoming push request into a {@link Build} and adds it
     * to the processing queue.
     *
     * @param job the incoming push request containing repository and commit information
     */
    public void queueJob(PushRequestDTO job) {
        Build build = Build.newBuild(
            job.after(), 
            job.getRepository().clone_url(), 
            job.getRepository().getOwner().getName()
        );
        queue.offer(build);

        buildRepository.save(build);
    }

    public void handleJob(Build job) { }

    /**
     * Initializes and starts the background worker thread.
     *
     * This method is automatically invoked by Spring after dependency injection
     * has completed.
     */
    @PostConstruct
    public void startThread() { 
        thread = new Thread(this::runThread, "ci_service_worker");
        thread.start();
    }

    /**
     * Stops the background worker thread gracefully.
     *
     * This method is automatically invoked by Spring during application shutdown.
     * The worker thread is interrupted to allow safe termination.
     */
    @PreDestroy
    public void stopThread() { 
        thread.interrupt();
    }

    /**
     * Main loop executed by the worker thread.
     *
     * The thread continuously waits for new jobs from the queue and processes
     * them one at a time. If interrupted, the loop exits gracefully.
     */
    private void runThread() { 
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Build job = queue.take();
                handleJob(job);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
