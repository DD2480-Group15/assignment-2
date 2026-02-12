package se.kth.dd2480.group15.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.kth.dd2480.group15.api.dto.response.BuildListResponse;
import se.kth.dd2480.group15.api.dto.response.BuildLogResponse;
import se.kth.dd2480.group15.api.dto.response.BuildMetaResponse;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.domain.BuildSummary;
import se.kth.dd2480.group15.domain.LogFile;
import se.kth.dd2480.group15.infrastructure.persistence.BuildRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class BuildServiceTest {

    @Mock
    private BuildRepository buildRepository;

    @InjectMocks
    private BuildService buildService;

    /**
     * Initializes Mockito mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Verifies that when builds exist in the repository, calling {@code getAllBuilds}
     * returns a {@link BuildListResponse} containing the same build summaries.
     * <p>
     * Test setup:
     * - A {@link BuildSummary} is created.
     * - The repository {@code listAll} method is stubbed to return a list containing the summary.
     * - {@code getAllBuilds} is invoked, and the returned response is verified to contain the same summary.
     */
    @Test
    void getAllBuilds_repositoryReturnsSummaries_returnsResponseContainingSummaries() {
        BuildSummary summary = new BuildSummary(
                UUID.randomUUID(),
                "adb123",
                Instant.now()
        );
        when(buildRepository.listAll()).thenReturn(List.of(summary));

        BuildListResponse response = buildService.getAllBuilds();

        assertNotNull(response);
        List<BuildSummary> builds = response.builds();
        assertEquals(1, builds.size());
        assertEquals(summary, builds.get(0));
    }

    /**
     * Verifies that when a build exists in the repository, calling {@code getBuild}
     * returns a {@link BuildMetaResponse} mapped from the stored {@link Build}.
     * <p>
     * Test setup:
     * - A new {@link Build} is created.
     * - The repository {@code findById} method is stubbed to return the build for its id.
     * - {@code getBuild} is invoked, and the returned response fields are verified to match the build.
     */
    @Test
    void getBuild_existingBuild_returnsMappedBuildMetaResponse() {
        Build build = Build.newBuild("adb123", "some-url", "some-owner", "some-repo");
        when(buildRepository.findById(build.getBuildId())).thenReturn(Optional.of(build));

        BuildMetaResponse response = buildService.getBuild(build.getBuildId());

        assertNotNull(response);
        assertEquals(build.getBuildId(), response.buildId());
        assertEquals(build.getCommitSha(), response.commitSha());
        assertEquals(build.getRepoOwner(), response.owner());
        assertEquals(build.getStatus(), response.status());
        assertEquals(build.getCreatedAt(), response.createdAt());
    }

    /**
     * Verifies that when a build log exists in the repository, calling {@code getBuildLog}
     * returns a {@link BuildLogResponse} containing the log content.
     * <p>
     * Test setup:
     * - A build id is created.
     * - A {@link LogFile} is created.
     * - The repository {@code getLog} method is stubbed to return the log file for the build id.
     * - {@code getBuildLog} is invoked, and the returned response is verified to contain the expected content.
     */
    @Test
    void getBuildLog_existingLog_returnsResponseContainingLogContent() {
        UUID buildId = UUID.randomUUID();
        LogFile logFile = new LogFile("Sample build log content");
        when(buildRepository.getLog(buildId)).thenReturn(Optional.of(logFile));

        BuildLogResponse response = buildService.getBuildLog(buildId);

        assertNotNull(response);
        assertEquals(logFile.content(), response.logContent());
    }
}