package se.kth.dd2480.group15.services;

import org.springframework.stereotype.Service;
import se.kth.dd2480.group15.api.dto.response.BuildListResponse;
import se.kth.dd2480.group15.api.dto.response.BuildLogResponse;
import se.kth.dd2480.group15.api.dto.response.BuildMetaResponse;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.domain.BuildNotFoundException;
import se.kth.dd2480.group15.domain.BuildSummary;
import se.kth.dd2480.group15.domain.LogFile;
import se.kth.dd2480.group15.infrastructure.persistence.BuildRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class responsible for handling operations related to retrieval of build
 * metadata and logs. Provides methods for retrieving a list of all builds, individual
 * build details, and their associated logs.
 */
@Service
public class BuildService {

    private final BuildRepository buildRepository;

    /**
     * Constructs a new instance of the BuildService.
     * This service is responsible for handling operations related to build metadata
     * and logs by interacting with the BuildRepository.
     *
     * @param buildRepository the repository used for managing build entities and their associated data
     */
    public BuildService(BuildRepository buildRepository) {
        this.buildRepository = buildRepository;
    }

    /**
     * Retrieves a response containing a list of all build summaries.
     *
     * @return a {@link BuildListResponse} object containing a list of builds with
     *         metadata such as build ID, commit hash, and creation timestamp
     */
    public BuildListResponse getAllBuilds() {
        List<BuildSummary> builds = buildRepository.listAll();

        return new BuildListResponse(builds);
    }

    /**
     * Retrieves metadata for a specific build based on its unique identifier.
     *
     * @param buildId the unique identifier of the build to retrieve
     * @return a {@link BuildMetaResponse} object containing metadata such as the build ID,
     *         commit SHA, repository owner, status, and creation timestamp
     * @throws BuildNotFoundException if no build with the specified ID is found
     */
    public BuildMetaResponse getBuild(UUID buildId) {
        Optional<Build> result = buildRepository.findById(buildId);

        if (result.isEmpty()) throw new BuildNotFoundException(buildId);
        Build build = result.get();

        return new BuildMetaResponse(
                build.getBuildId(),
                build.getCommitSha(),
                build.getRepoOwner(),
                build.getStatus(),
                build.getCreatedAt()
        );
    }

    /**
     * Retrieves the log output for a specific build based on its unique identifier.
     *
     * @param buildId the unique identifier of the build whose log is to be retrieved
     * @return a {@link BuildLogResponse} object containing the log content of the specified build
     * @throws BuildNotFoundException if no build or associated log is found with the given identifier
     */
    public BuildLogResponse getBuildLog(UUID buildId) {
        Optional<LogFile> log = buildRepository.getLog(buildId);

        if (log.isEmpty()) throw new BuildNotFoundException(buildId);

        return new BuildLogResponse(log.toString());
    }
}
