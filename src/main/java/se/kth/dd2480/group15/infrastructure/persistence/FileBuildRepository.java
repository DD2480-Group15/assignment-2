package se.kth.dd2480.group15.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Repository;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.infrastructure.config.StorageProperties;
import se.kth.dd2480.group15.domain.BuildSummary;
import se.kth.dd2480.group15.infrastructure.entity.BuildMetaFile;
import se.kth.dd2480.group15.domain.LogFile;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private final ConcurrentHashMap<Path, Object> fileLocks = new ConcurrentHashMap<>();

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
            if (!Files.exists(indexFile)) {
                Files.createFile(indexFile);
            }
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
        String metaJsonString = toJsonString(metaFile);

        boolean isNewBuild = tryCreateDirectory(buildDir);
        if (isNewBuild) {
            BuildSummary entry = new BuildSummary(
                    build.getBuildId(),
                    build.getCommitSha(),
                    build.getCreatedAt()
            );
            atomicWriteToFile(metaPath, metaJsonString);
            appendLineToFile(indexFile, toJsonString(entry)); // Only append to index after metadata has been persisted
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

    private String toJsonString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize build into a build index entry: " + object, e);
        }
    }

    private <T> T fromJsonString(String json, Class<T> c) {
        try {
            return MAPPER.readValue(json, c);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize build index entry into a build: " + json, e);
        }
    }

    private Object lockFor(Path path) {
        return fileLocks.computeIfAbsent(path.toAbsolutePath().normalize(), p -> new Object());
    }

    private void atomicWriteToFile(Path file, String content) {
        /*
        1. Create a temporary file
        2. Get lock for temp file
        3. Write to temp file
        4. Atomically move/copy the content of the temp file to the actual file

        Prevents partially written meta.json if the process crashes mid-write
        or if the meta.json is accessed mid-write.
         */
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");

        Object lock = lockFor(file);
        synchronized (lock) {
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
        appendLineToFile(logPath, chunk);
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

    /**
     * Retrieves a list of all build entries from the index file.
     *
     * @return a list of builds
     */
    @Override
    public List<BuildSummary> listAll() {
        List<BuildSummary> builds = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(indexFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                BuildSummary entry = fromJsonString(line, BuildSummary.class);
                builds.add(entry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return builds;
    }

    /**
     * Retrieves a build instance by its unique identifier.
     * <p>
     * If the directory exists, it reads the metadata file, parses the metadata into
     * a {@link Build} object, and returns it. If the directory does not exist, an
     * empty {@link Optional} is returned. If an I/O error occurs when reading the
     * file, an exception is thrown.
     *
     * @param buildId the unique identifier of the build to be retrieved
     * @return an {@link Optional} containing the {@code Build} instance if found,
     *         or an empty {@link Optional} if the build does not exist
     * @throws RuntimeException if an error occurs while reading the metadata file
     */
    @Override
    public Optional<Build> findById(UUID buildId) {
        Path buildDir = getBuildDirectory(buildId);
        Path metaPath = buildDir.resolve(META_FILE_NAME);

        // Check if the build exists
        if (!Files.isDirectory(buildDir)) return Optional.empty();

        try {
            String jsonString = Files.readString(metaPath);
            BuildMetaFile metaFile = fromJsonString(jsonString, BuildMetaFile.class);
            return Optional.of(metaFile.toDomain());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the log file associated with a specified build ID.
     * <p>
     * If the log file exists, an {@link Optional} containing a {@link LogFile} object
     * is returned. If there is no log or the directory does not exist, an empty
     * {@link Optional} is returned. If an I/O error occurs while reading the log, a
     * {@link RuntimeException} is thrown.
     *
     * @param buildId the unique identifier of the build whose log is being retrieved
     * @return an {@link Optional} containing the {@link LogFile} if the log exists,
     *         or an empty {@link Optional} if no log exists for the specified build ID
     * @throws RuntimeException if an I/O error occurs when reading the log
     */
    @Override
    public Optional<LogFile> getLog(UUID buildId) {
        Path buildDir = getBuildDirectory(buildId);
        Path logPath = buildDir.resolve(LOG_FILE_NAME);

        // Check if the build exists
        if (!Files.isDirectory(buildDir)) return Optional.empty();

        try (BufferedReader reader = Files.newBufferedReader(logPath)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return Optional.of(new LogFile(sb.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
