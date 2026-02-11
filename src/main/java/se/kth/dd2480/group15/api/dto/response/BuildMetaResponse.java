package se.kth.dd2480.group15.api.dto.response;

import se.kth.dd2480.group15.domain.Build;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing the metadata of a single build in the history list.
 *
 * @param buildId   the unique session ID for a specific build
 * @param commitSha the 40-character hex hash identifying the git commit
 * @param owner     the GitHub username of the person who pushed the code
 * @param status    the final outcome of the build (SUCCESS, FAILURE, ERROR)
 * @param createdAt the timestamp of when the build was triggered
 */
public record BuildMetaResponse(
        UUID buildId,
        String commitSha,
        String owner,
        Build.Status status,
        Instant createdAt
) { }