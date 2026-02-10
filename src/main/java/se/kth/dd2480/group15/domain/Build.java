package se.kth.dd2480.group15.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a build process for a specific commit in a repository.
 * Keeps track of build status and time information.
 */
public class Build {

    /**
     * Represents the current state of the build process.
     */
    public enum Status {
        QUEUED, RUNNING, SUCCESS, FAILED
    }

    private final UUID buildId;
    private final String commitSha;
    private final String repoUrl;
    private final String repoOwner;
    private final Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
    private Status status;

    /**
     * Method to create a new build with a generated ID and current timestamp.
     *
     * @param commitSha the unique identifier of the commit to be built
     * @param repoUrl the URL of the repository containing the code
     * @return a new Build instance in QUEUED status
     */
    public static Build newBuild(String commitSha, String repoUrl, String repoOwner) {
        return new Build(UUID.randomUUID(), commitSha, repoUrl, repoOwner, Status.QUEUED, Instant.now(), null, null);
    }

    /**
     * Recreates an existing build object, used when loading data from storage.
     *
     * @param buildId the existing UUID of the build
     * @param commitSha the commit identifier
     * @param repoUrl the repository URL
     * @param repoOwner the owner of the repository
     * @param status the current status of the build
     * @param createdAt timestamp of when the build was first created
     * @param startedAt timestamp of when the build started, or null if not started
     * @param finishedAt timestamp of when the build finished, or null if not finished
     * @return a Build instance reflecting the provided state
     */
    public static Build rehydrate(
            UUID buildId,
            String commitSha,
            String repoUrl,
            String repoOwner,
            Status status,
            Instant createdAt,
            Instant startedAt,
            Instant finishedAt) {
        return new Build(buildId, commitSha, repoUrl, repoOwner, status, createdAt, startedAt, finishedAt);
    }

    private Build(
            UUID buildId,
            String commitSha,
            String repoUrl,
            String repoOwner,
            Status status,
            Instant createdAt,
            Instant startedAt,
            Instant finishedAt) {
        this.buildId = buildId;
        this.commitSha = commitSha;
        this.repoUrl = repoUrl;
        this.repoOwner = repoOwner;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    /** @return the unique identifier for this build */
    public UUID getBuildId() { return buildId; }
    
    /** @return the commit SHA associated to this build */
    public String getCommitSha() { return commitSha; }

    /** @return the URL of the repository */
    public String getRepoUrl() { return repoUrl; }

    /** @return the owner of the repository */
    public String getRepoOwner() { return repoOwner; }

    /** @return the current status of the build */
    public Status getStatus() { return status; }

    /** @return the timestamp of when the build was created */
    public Instant getCreatedAt() { return createdAt; }

    /** @return the timestamp of when the build started, or null */
    public Instant getStartedAt() { return startedAt; }

    /** @return the timestamp of when the build finished, or null */
    public Instant getFinishedAt() { return finishedAt; }

    /**
     * Updates the build status to RUNNING and sets the start timestamp to now.
     */
    public void startBuild() {
        this.startedAt = Instant.now();
        this.status = Status.RUNNING;
    }

    /**
     * Updates the build status to FAILED and sets the finish timestamp to now.
     */
    public void failBuild() {
        this.finishedAt = Instant.now();
        this.status = Status.FAILED;
    }

    /**
     * Updates the build status to SUCCESS and sets the finish timestamp to now.
     */
    public void finishBuild() {
        this.finishedAt = Instant.now();
        this.status = Status.SUCCESS;
    }
}