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
}