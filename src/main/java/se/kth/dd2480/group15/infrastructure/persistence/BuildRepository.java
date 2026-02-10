package se.kth.dd2480.group15.infrastructure.persistence;

import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.infrastructure.entity.LogSlice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a repository for managing Build entities and their associated data.
 * This interface provides methods for persisting build metadata, handling build logs,
 * and retrieving information about builds stored in the system.
 */
public interface BuildRepository {

    /**
     * Persists the metadata of the given build.
     * <p>
     * If metadata for the build already exists, it is replaced.
     * Otherwise, a new metadata entry is created.
     *
     * @param build the build whose metadata should be persisted
     */
    void save(Build build);

    /**
     * Appends a log-chunk to the log associated with the specified build id.
     *
     * @param buildId the unique identifier for the build whose log is being updated
     * @param chunk the log-chunk that will be appended to the build's log
     */
    void appendToLog(UUID buildId, String chunk);

    /**
     * Retrieves a list of at most {@code limit} builds, starting from the specified
     * offset in relation to the most recent build.
     * <p>
     * Builds are ordered by creation time descending (most recent first).
     *
     * @param limit  maximum number of builds to return
     * @param offset specifies the index of the first build to return, where
     *               {@code offset = 0} refers to the most recent build
     * @return a list of {@link Build} objects representing a slice of the stored builds
     */
    List<Build> list(int limit, int offset);

    /**
     * Retrieves the build associated with the specified build id.
     *
     * @param buildId the unique identifier of the build whose metadata is to be retrieved
     * @return a {@link Build} object containing the metadata for the specified buildId
     */
    Optional<Build> findById(UUID buildId);

    /**
     * Retrieves a portion of the log associated with the specified build, starting from
     * the given offset. This method reads a segment of the log and provides the content
     * along with the next offset indicating where to begin reading the next segment.
     *
     * @param buildId the unique identifier for the build whose log is being retrieved
     * @param offset  the position within the content file to start reading from
     * @return a {@link LogSlice} object containing the extracted log content and the
     *                            offset for the next log segment
     */
    LogSlice getLog(UUID buildId, long offset);
}
