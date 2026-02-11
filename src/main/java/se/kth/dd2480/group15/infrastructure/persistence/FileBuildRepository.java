package se.kth.dd2480.group15.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Repository;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.infrastructure.config.StorageProperties;
import se.kth.dd2480.group15.infrastructure.entity.BuildIndexEntry;
import se.kth.dd2480.group15.infrastructure.entity.BuildMetaFile;
import se.kth.dd2480.group15.domain.LogFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * FileBuildRepository is a file-based implementation of the {@link BuildRepository}
 * that manages build metadata and logs by storing them in the file system.
 */
@Repository
public class FileBuildRepository implements BuildRepository {

    public static final String INDEX_FILE_NAME = "index.jsonl";
    public static final String META_FILE_NAME = "meta.json";
    public static final String LOG_FILE_NAME = "build.log";

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private final Path buildRoot;
    private final Path indexFile;

    /**
     * Constructs a new instance of the FileBuildRepository.
     * <p>
     * This constructor initializes the repository by setting up a directory on the
     * filesystem to store build-related data if it does not exist already. The
     * directory serves as the root location for build artifacts and metadata.
     * <p>
     * Any failure to create the necessary directories will result in a runtime exception.
     */
    public FileBuildRepository(StorageProperties storageProperties) {
        this.buildRoot = Paths.get(storageProperties.buildsRoot()).toAbsolutePath().normalize();
        this.indexFile = buildRoot.resolve(INDEX_FILE_NAME);

        try {
            Files.createDirectories(buildRoot); // Creates ~/dd2480-ci/builds if it doesn't already exist
        } catch (IOException e) {
            throw new RuntimeException("Failed to create builds path directory: " + buildRoot, e);
        }
    }

    /**
     * Saves a {@link Build} instance to the file system. If the build is new, a new
     * directory is created to store its metadata, and the build is appended to the index file.
     * For existing builds, the metadata file is updated.
     *
     * @param build the {@link Build} object containing build information to be saved
     */
    @Override
    public void save(Build build) {
        Path buildDir = getBuildDirectory(build.getBuildId());
        Path metaPath = buildDir.resolve(META_FILE_NAME);

        BuildMetaFile metaFile = BuildMetaFile.from(build);
        String metaJsonString = toJson(metaFile);

        boolean isNewBuild = tryCreateDirectory(buildDir);
        if (isNewBuild) {
            BuildIndexEntry entry = new BuildIndexEntry(
                    build.getBuildId(),
                    build.getCommitSha(),
                    build.getCreatedAt()
            );
            atomicWriteToFile(metaPath, metaJsonString);
            appendLineToFile(indexFile, toJson(entry)); // Only append to index after metadata has been persisted
        }
        else {
            atomicWriteToFile(metaPath, metaJsonString);
        }
    }

    private Path getBuildDirectory(UUID buildId) {
        return buildRoot.resolve(buildId.toString());
    }

    private boolean tryCreateDirectory(Path dir) {
        try {
            Files.createDirectory(dir);
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create build directory: " + dir, e);
        }
    }

    private String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize build into a build index entry: " + object, e);
        }
    }

    /**
     * Write text by replacing the file atomically (best effort).
     * Prevents partially written meta.json if the process crashes mid-write
     * or if the meta.json is accessed mid-write.
     */
    private void atomicWriteToFile(Path file, String content) {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");

        try {
            Files.writeString(
                    tmp,
                    content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // If atomic move doesn't work
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write meta file: " + file, ex);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write meta file: " + file, e);
        }
    }

    /**
     * Appends a log chunk to the log file associated with the specified build id.
     * If the build directory does not exist, an exception is thrown.
     *
     * @param buildId the unique identifier of the build whose log is being updated
     * @param chunk the log chunk to append to the build's log
     * @return {@code true} if the log chunk was successfully appended; {@code false}
     *         if the build directory does not exist
     */
    @Override
    public boolean appendToLog(UUID buildId, String chunk) {
        Path buildDir = getBuildDirectory(buildId);

        if (!Files.isDirectory(buildDir)) {
            return false;
        }

        Path logPath = buildDir.resolve(LOG_FILE_NAME);
        appendToFile(logPath, chunk);
        return true;
    }

    private void appendLineToFile(Path file, String line) {
        appendToFile(file, line + System.lineSeparator());
    }

    private void appendToFile(Path file, String content) {
        try {
            Files.writeString(file, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to file: " + file, e);
        }
    }

    @Override
    public List<Build> list(int limit, int offset) {
        return List.of();
    }

    @Override
    public Optional<Build> findById(UUID buildId) {
        return Optional.empty();
    }

    @Override
    public Optional<LogFile> getLog(UUID buildId) {
        return Optional.empty();
    }
}
