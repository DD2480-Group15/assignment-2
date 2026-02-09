package se.kth.dd2480.group15.domain;

import java.time.Instant;
import java.util.UUID;

public class Build {

    public enum Status {
        QUEUED, RUNNING, SUCCESS, FAILED
    }

    private final UUID buildId;
    private final String commitSha;
    private final String repoUrl;
    private final Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
    private Status status;

    public static Build newBuild(String commitSha, String repoUrl) {
        return new Build(UUID.randomUUID(), commitSha, repoUrl, Status.QUEUED, Instant.now(), null, null);
    }

    public static Build rehydrate(
            UUID buildId,
            String commitSha,
            String repoUrl,
            Status status,
            Instant createdAt,
            Instant startedAt,
            Instant finishedAt) {
        return new Build(buildId, commitSha, repoUrl, status, createdAt, startedAt, finishedAt);
    }

    private Build(
            UUID buildId,
            String commitSha,
            String repoUrl,
            Status status,
            Instant createdAt,
            Instant startedAt,
            Instant finishedAt) {
        this.buildId = buildId;
        this.commitSha = commitSha;
        this.repoUrl = repoUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    // Getters
    public UUID getBuildId()       { return buildId; }
    public String getCommitSha()   { return commitSha; }
    public String getRepoUrl()     { return repoUrl; }
    public Status getStatus()      { return status; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getStartedAt()  { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }

    public void startBuild() {
        this.startedAt = Instant.now();
        this.status = Status.RUNNING;
    }

    public void failBuild() {
        this.finishedAt = Instant.now();
        this.status = Status.FAILED;
    }

    public void finishBuild() {
        this.finishedAt = Instant.now();
        this.status = Status.SUCCESS;
    }
}