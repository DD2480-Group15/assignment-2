package se.kth.dd2480.group15.infrastructure.persistence;

import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.domain.BuildSummary;
import se.kth.dd2480.group15.domain.LogFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a repository for managing Build entities and their associated data.
 * This interface provides methods for persisting build metadata, handling build logs,
 * and retrieving build information stored in the system.
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
     * @return {@code true} if the log chunk was successfully appended; {@code false} if
     *         the build directory does not exist
     */
    boolean appendToLog(UUID buildId, String chunk);

    /**
     * Retrieves a list of all build instances.
     * <p>
     * Builds are ordered by creation date (oldest first).
     *
     * @return a list of {@link Build} objects representing all build instances
     */
    List<BuildSummary> listAll();

    /**
     * Retrieves the build associated with the specified build id.
     *
     * @param buildId the unique identifier of the build whose metadata is to be retrieved
     * @return an {@link Optional} containing the {@link Build} entity if found, or an
     *         empty Optional if no build exists with the given id
     */
    Optional<Build> findById(UUID buildId);

    /**
     * Retrieves the log associated with the specified build.
     *
     * @param buildId the unique identifier for the build whose log is being retrieved
     * @return an {@link Optional} containing a {@link LogFile} object if the log exists,
     *         or an empty Optional if no log exists for the given build id.
     */
    Optional<LogFile> getLog(UUID buildId);
}
