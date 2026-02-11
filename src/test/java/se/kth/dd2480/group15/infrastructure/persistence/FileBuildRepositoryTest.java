package se.kth.dd2480.group15.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.domain.BuildSummary;
import se.kth.dd2480.group15.domain.LogFile;
import se.kth.dd2480.group15.infrastructure.entity.BuildMetaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FileBuildRepositoryTest {

    @TempDir
    static Path tempDir;

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("ci.storage.builds-root", () -> tempDir.toString());
    }

    @Autowired
    private BuildRepository repo;

    /**
     * Verifies that when storing a new build, a directory is created for it, its
     * metadata is stored under {@code builds/{buildId}/meta.json}, and an entry is added
     * to the index file for the build.
     * <p>
     * Test setup:
     * - A new build is created with a commit SHA, repository URL, repository owner, and repository name.
     * - The created build is stored using the save method of the repository class.
     *
     * @throws IOException if any file operations fail during the test
     */
    @Test
    void save_newBuild_createsDirectoriesAndIndexEntry() throws IOException {
        Build build = Build.newBuild("abc123", "url", "this", "name456");
        UUID buildId = build.getBuildId();

        repo.save(build);

        // Directory created
        Path buildDir = tempDir.resolve(buildId.toString());
        assertTrue(Files.isDirectory(buildDir));

        // Metadata stored
        Path metaPath = buildDir.resolve(FileBuildRepository.META_FILE_NAME);
        BuildMetaFile expectedMetadata = BuildMetaFile.from(build);
        String actualMetaJsonString = Files.readString(metaPath);
        BuildMetaFile actualMetadata = MAPPER.readValue(actualMetaJsonString, BuildMetaFile.class);
        assertEquals(expectedMetadata, actualMetadata);

        // Entry added to index file
        Path indexPath = tempDir.resolve(FileBuildRepository.INDEX_FILE_NAME);
        assertTrue(Files.exists(indexPath));
        List<String> builds = Files.readAllLines(indexPath);
        BuildSummary expectedIndexEntry = new BuildSummary(
                buildId,
                build.getCommitSha(),
                build.getCreatedAt());
        BuildSummary actualIndexEntry = MAPPER.readValue(
                builds.get(builds.size() - 1),
                BuildSummary.class);
        assertEquals(expectedIndexEntry, actualIndexEntry);
    }

    /**
     * Verifies that when appending a log chunk to the log, the {@code build.log} file
     * is created/exists, and the log chunk is written to the log file.
     * <p>
     * Test setup:
     * - A new build is created with a specific commit SHA, repository URL, repository owner, and repository name.
     * - The build is saved to storage.
     * - A log chunk is appended to the build's log file using the repository's appendToLog method.
     *
     * @throws IOException if any file operations fail during the test
     */
    @Test
    void appendToLog_addLogChunk_chunkIsAppendedToFile() throws IOException {
        Build build = Build.newBuild("abc123", "url", "this", "name456");
        UUID buildId = build.getBuildId();
        String chunk = "chunk";

        repo.save(build);
        boolean success = repo.appendToLog(buildId, chunk);

        // Log file created and chunk appended to the end
        Path logPath = tempDir.resolve(buildId.toString()).resolve(FileBuildRepository.LOG_FILE_NAME);
        assertTrue(Files.exists(logPath));
        List<String> log = Files.readAllLines(logPath);
        String actualLogString = log.get(log.size() - 1);
        assertTrue(success);
        assertEquals(chunk, actualLogString);
    }

    /**
     * Verifies that when saving a new build, calling listAll returns a list where
     * the build summary of the saved build is the last entry.
     * <p>
     * Test setup:
     * - A new build is created.
     * - The build is saved to the repository using the {@code save} method.
     * - listAll is invoked, and the last entry is verified to match the expected build summary.
     */
    @Test
    void listAll_afterSavingBuild_returnsBuildSummary() {
        Build build = Build.newBuild("abc123", "url", "this", "name456");
        UUID buildId = build.getBuildId();

        repo.save(build);

        List<BuildSummary> builds = repo.listAll();
        assertFalse(builds.isEmpty());

        BuildSummary lastEntry = builds.get(builds.size() - 1);
        BuildSummary expectedSummary = new BuildSummary(
                buildId,
                build.getCommitSha(),
                build.getCreatedAt()
        );

        assertEquals(expectedSummary, lastEntry);
    }

    /**
     * Tests the functionality of the {@code findById} method in the {@code repo} repository class.
     * Verifies that a build that has been saved can be correctly retrieved using its unique identifier.
     * <p>
     * Test setup:
     * - A new build is created.
     * - The build is saved to the repository using the {@code save} method.
     * - The build is retrieved by its unique identifier using the {@code findById} method.
     */
    @Test
    void findById_whenBuildExists_returnsBuild() {
        Build build = Build.newBuild("abc123", "url", "this", "name456");
        UUID buildId = build.getBuildId();

        repo.save(build);

        // Retrieve the build by ID
        Build retrievedBuild = repo.findById(buildId).orElseThrow();

        // Verify the retrieved build matches the original
        assertEquals(buildId, retrievedBuild.getBuildId());
        assertEquals(build.getCommitSha(), retrievedBuild.getCommitSha());
        assertEquals(build.getRepoUrl(), retrievedBuild.getRepoUrl());
        assertEquals(build.getRepoOwner(), retrievedBuild.getRepoOwner());
        assertEquals(build.getRepoName(), retrievedBuild.getRepoName());
        assertEquals(build.getCreatedAt(), retrievedBuild.getCreatedAt());
    }

    /**
     * Verifies that after appending a log entry to a build's log file, the log
     * entry is correctly stored and can then be retrieved.
     * <p>
     * Test setup:
     * - A new build is created.
     * - The build is saved to the repository.
     * - A log chunk is appended to the build's log file.
     * - The log can be retrieved and contains the stored log chunk.
     */
    @Test
    void getLog_afterAppendingLog_returnsLogContainingChunk() {
        Build build = Build.newBuild("abc123", "url", "this", "name456");
        UUID buildId = build.getBuildId();
        String logChunk = "Test log entry";

        // Save the build and append a log chunk
        repo.save(build);
        boolean appendSuccess = repo.appendToLog(buildId, logChunk);

        // Verify the log content
        assertTrue(appendSuccess);
        LogFile logFile = repo.getLog(buildId).orElseThrow();
        assertNotNull(logFile);
        assertTrue(logFile.content().contains(logChunk));
    }
}