package se.kth.dd2480.group15.infrastructure.entity;

import se.kth.dd2480.group15.domain.Build;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents metadata for to a build process.
 *
 * @param buildId the unique identifier of the build
 * @param commitSha the commit hash associated with the build
 * @param repoUrl the URL of the repository containing the code
 * @param repoOwner the owner of the repository
 * @param status the current status of the build
 * @param createdAt the timestamp when the build was created
 * @param startedAt the timestamp when the build started, or null if not started
 * @param finishedAt the timestamp when the build finished, or null if not finished
 */
public record BuildMetaFile(
        UUID buildId,
        String commitSha,
        String repoUrl,
        String repoOwner,
        Build.Status status,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt
) {
    /**
     * Transforms a {@link Build} object into a {@link BuildMetaFile} instance.
     *
     * @param b the build instance to convert
     * @return a new {@link BuildMetaFile} instance with metadata from the provided build
     */
    public static BuildMetaFile from(Build b) {
        return new BuildMetaFile(
                b.getBuildId(),
                b.getCommitSha(),
                b.getRepoUrl(),
                b.getRepoOwner(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getStartedAt(),
                b.getFinishedAt()
        );
    }

    /**
     * Converts this BuildMetaFile instance into its corresponding {@link Build} domain object.
     *
     * @return a Build instance reflecting the state of this BuildMetaFile
     */
    public Build toDomain() {
        return Build.rehydrate(
                buildId, commitSha, repoUrl, repoOwner,
                status, createdAt, startedAt, finishedAt
        );
    }
}
