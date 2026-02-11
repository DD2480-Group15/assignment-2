package se.kth.dd2480.group15.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a summary of a build instance.
 *
 * @param buildId the unique identifier of the build
 * @param commitSha the hash of the commit associated with the build
 * @param createdAt the timestamp when the build was created
 */
public record BuildSummary(
        UUID buildId,
        String commitSha,
        Instant createdAt
) { }
