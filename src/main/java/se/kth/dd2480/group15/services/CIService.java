package se.kth.dd2480.group15.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import se.kth.dd2480.group15.api.dto.request.PushRequestDTO;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.application.services.NotifierService;
import se.kth.dd2480.group15.infrastructure.persistence.BuildRepository;

/**
 * Continuous Integration service responsible for handling incoming build jobs.
 * 
 * The service maintains an internal job queue and processes builds asynchronously
 * using a dedicated worker thread.
 * 
 * Build logs are persisted continuously during execution in the {@link ProcessRunner} 
 * and the final result is sent through the {@link NotifierService}.
 */
@Service
public class CIService {

    /** Queue holding incoming build jobs waiting to be processed. */
    private BlockingQueue<Build> queue = new LinkedBlockingQueue<>();

    /** Background worker thread responsible for handling queued builds. */
    private Thread thread;

    /** Service responsible for executing clone, build and test processes. */
    private final ProcessRunner processRunner;

    /** Service responsible for notifying external systems about build results. */
    private final NotifierService notifierService;

    /** Repository used for persisting build logs and metadata. */
    private final BuildRepository buildRepository;

    /**
     * Creates a new CIService.
     * 
     * @param processService service used to execute clone, build and test commands
     * @param notifierService service used to notify external systems of build results
     * @param buildRepository repository used to persist build logs and state
     */
    public CIService(ProcessRunner processRunner, NotifierService notifierService, BuildRepository buildRepository) {
        this.processRunner = processRunner;
        this.notifierService = notifierService;
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

    /**
     * Handles execution of a single build job. The life cycle includes cloning, building, 
     * testing and cleanup. Logs are appended continuously during execution. Then finished, 
     * the build status is updated and a notification is sent.
     * 
     * @param job the build job to process
     */
    public void handleJob(Build job) {
        job.startBuild();
        buildRepository.save(job);

        boolean success;
        StringBuilder sb = new StringBuilder();

        // Clone repository
        success = processRunner.cloneRepo(job, event -> buildRepository.appendToLog(job.getBuildId(), event));
        sb.append("Clone: ").append(success ? "Success" : "Fail").append("\n");

        if (success) {
            // If clone success, build
            success = processRunner.build(job, event -> buildRepository.appendToLog(job.getBuildId(), event));
            sb.append("Build: ").append(success ? "Success" : "Fail").append("\n");

            if (success) {
                // If build success, test
                success = processRunner.test(job, event -> buildRepository.appendToLog(job.getBuildId(), event));
                sb.append("Test: ").append(success ? "Success" : "Fail").append("\n");
            }
        }

        // Remove last \n
        sb.deleteCharAt(sb.length()-1);

        processRunner.cleanup(job);

        // Set build status and finish time
        if (success) { job.finishBuild(); }
        else { job.failBuild(); }

        // Log meta data
        buildRepository.save(job);

        // Call Notifier
        String state = success ? "success" : "fail", description = sb.toString();
        notifierService.notify(job.getRepoOwner(), job.getRepoUrl(), job.getCommitSha(), state, description);
    }

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
