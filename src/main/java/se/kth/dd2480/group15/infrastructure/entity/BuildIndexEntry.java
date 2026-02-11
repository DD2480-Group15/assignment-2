package se.kth.dd2480.group15.infrastructure.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an entry in the build index.
 *
 * @param buildId the unique identifier of the build
 * @param commitHash the hash of the commit associated with the build
 * @param createdAt the timestamp when the build entry was created
 */
public record BuildIndexEntry(
        UUID buildId,
        String commitHash,
        Instant createdAt
) { }
