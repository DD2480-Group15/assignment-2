package se.kth.dd2480.group15.services;

import org.springframework.stereotype.Service;
import se.kth.dd2480.group15.domain.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * This is a service responsible for executing system processes related to the CI pipeline
 * 
 * Handles Git operations (clone, checkout) and Maven operations (build, test)
 * Output from these processes is streamed back to the caller via a {@link Consumer}
 */
@Service
public class ProcessRunner {

    /**
     * Default constructor for the spring dependency injection
     */
    public ProcessRunner() {
    }

    /**
     * Clones the repository and checks out the specific commit SHA provided in the build job
     *
     * @param job   The build job containing repository URL and commit SHA etc
     * @param onLog Of consumer type that receives each line of output from the git process
     * @return {@code true} if both clone and checkout were successful (exit code will be 0), {@code false} if not
     */
    public boolean cloneRepo(Build job, Consumer<String> onLog) {
        Path workspacePath = Path.of("workspace", job.getBuildId().toString());   // should work same on win,mac,linux
       
        // Git clone
        onLog.accept(job.getRepoUrl());
        ProcessBuilder clonePb = new ProcessBuilder("git", "clone", job.getRepoUrl(), workspacePath.toString());
        // use helper func runProcess
        boolean cloneSuccess = runProcess(clonePb, onLog);

        if (!cloneSuccess) {
            return false;   // exit on clone fail
        }

        // Git Checkout
        onLog.accept(job.getCommitSha());
        ProcessBuilder checkoutPb = new ProcessBuilder("git", "checkout", job.getCommitSha());
        checkoutPb.directory(workspacePath.toFile());

        return runProcess(checkoutPb, onLog);
    }

    /**
     * Compiles the project using the maven wrapper
     *
     * @param job   Build job to compile
     * @param onLog A consumer that receives each line of output from the maven process
     * @return {@code true} if compilation succeeded, {@code false} if not
     */
    public boolean build(Build job, Consumer<String> onLog) {
        Path workspacePath = Path.of("workspace", job.getBuildId().toString());

        // mvnw compile
        ProcessBuilder pbCompile = new ProcessBuilder("./mvnw", "compile");
        pbCompile.directory(workspacePath.toFile());
        return runProcess(pbCompile, onLog);
    }

    /**
     * This runs the project tests using the maven wrapper
     *
     * @param job   Build job to test
     * @param onLog Consumer that receives each line of output from the maven test process
     * @return {@code true} if all tests passed, otherwise it is {@code false} 
     */
    public boolean test(Build job, Consumer<String> onLog) {
        Path workspacePath = Path.of("workspace", job.getBuildId().toString());

        // mvnw test
        ProcessBuilder pbTest = new ProcessBuilder("./mvnw", "test");
        pbTest.directory(workspacePath.toFile());
        return runProcess(pbTest, onLog);
    }

    /**
     * Deletes the local workspace directory associated with specific build job
     *
     * @param job Build job whose workspace should be removed
     */
    public void cleanup(Build job) {
        java.io.File workspace = new java.io.File("workspace/" + job.getBuildId().toString());
        if (workspace.exists()) {
            deleteDirectory(workspace);
        }
    }

    /**
     * Helper to execute a process, redirect error streams and consume output line by line
     *
     * @param pb    ProcessBuilder to start
     * @param onLog The consumer for process output
     * @return {@code true} if the process finished with exit code 0
     */
    private boolean runProcess(ProcessBuilder pb, Consumer<String> onLog) {
        try {
            // Combine stdout and error stream to same place
            pb.redirectErrorStream(true); 
            Process process = pb.start();

            // open a reader to catch all of the process output data and send each line to Consumer
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    onLog.accept(line);
                }
            }

            // Wait until process is done and return true if we exit with code 0 = success!
            return process.waitFor() == 0;

        } catch (IOException | InterruptedException e) {
            onLog.accept("ERROR: Execution in ProcessRunner failed: " + e.getMessage());     // send error to logs
            return false;
        }
    }

    /**
     * Recursively deletes a directory with contents
     *
     * @param directory File or directory to delete
     */
    private void deleteDirectory(java.io.File directory) {
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }   
}