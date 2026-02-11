package se.kth.dd2480.group15.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.kth.dd2480.group15.domain.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcessRunnerTest {

    private ProcessRunner runner;
    private Build testJob;

    // this will run before each Test, setting up a new runner and valid testJob
    @BeforeEach
    void setUpTests() {
        runner = new ProcessRunner();
        
        // Creates a valid build object for the tests
        testJob = Build.newBuild(
            "553c207",
            "https://github.com/octocat/Hello-World",
            "octocat",
            "Hello-World"
        );

        // cleanup before a test starts to avoid "folder already exists" errors
        runner.cleanup(testJob);
    }

    /**
     * Negative test, catch that cloning fails correctly when the URL is invalid
     */
    @Test
    void shouldReturnFalseForInvalidRepo() {
        // Prepare a list to collect logs
        List<String> logs = new ArrayList<>();
        
        // Create a build with a URL that does not exist
        Build invalidJob = Build.newBuild("commit-sha", "www.invalid-url.here", "owner", "name");

        // Try to clone the invalid repo
        boolean result = runner.cloneRepo(invalidJob, line -> logs.add(line));

        // It should return false because the repo does not exist
        assertFalse(result);
        
        // Even if it fails git usually produces some error output
        assertTrue(logs.size() > 0);
    }

    /**
     * Verifies that the log consumer captures error messages from git
     */
    @Test
    void shouldCaptureOutputInConsumer() {
        List<String> logs = new ArrayList<>();
        
        // We use a real repo (githubs hello-world-repo) 
        // but a invalid commit-SHA to trigger a specific git behavior
        Build invalidShaJob = Build.newBuild("invalid-sha", "https://github.com/octocat/Hello-World", "octocat", "name");

        runner.cloneRepo(invalidShaJob, line -> logs.add(line));

        // Check if any line in the logs contains git error words: "fatal" or "error"
        boolean foundError = logs.stream().anyMatch(line -> line.contains("fatal") || line.contains("error"));
        
        assertTrue(foundError);
    }

    /**
     * Test cleanup, checks if the cleanup successfully deletes a directory
     * @throws Exception
     */
    @Test
    void cleanupShouldDeleteWorkspace() throws Exception {
        // Manually create a folder that looks like a workspace
        UUID id = testJob.getBuildId();
        File workspaceFolder = new File("workspace/" + id);
        workspaceFolder.mkdirs();
        
        // Create a dummy file inside the folder
        File dummyFile = new File(workspaceFolder, "dummy.txt");
        dummyFile.createNewFile();

        // Run the cleanup
        runner.cleanup(testJob);

        // The folder should be deleted now
        assertFalse(workspaceFolder.exists());
    }

    /**
     * Integration test, to verifying the full workflow:
     * 1. Successful cloning of a repository
     * 2. Attempt to execute the tests in the cloned workspace
     */
    @Test
    void verifyCloneAndTest() {
        List<String> logs = new ArrayList<>();
        
        // 1:
        // Verify successful clone from e real repo (git hello world)
        boolean cloneResult = runner.cloneRepo(testJob, line -> logs.add(line));
        assertTrue(cloneResult);
        
        File workspace = new File("workspace/" + testJob.getBuildId());
        assertTrue(workspace.exists()); // verify we have successfully cloned

        // 2:
        // Verify testing
        boolean testResult = runner.test(testJob, line -> logs.add(line));
        
        assertFalse(testResult); // expected fail since "hello world"-repo does not have maven wrapper

        boolean attemptedTestCommand = logs.stream().anyMatch(line -> line.contains("./mvnw test"));
        
        assertTrue(attemptedTestCommand); // verifies we correctly log the execution string "mwnw test"
    }

    /**
     * Verifies the testing phase by specifically ensuring it handles 
     * the commands executed correctly in a prepared workspace.
     */
    @Test
    void attemptToRunTestsInWorkspace() {
        List<String> logs = new ArrayList<>();
        File workspace = new File("workspace/" + testJob.getBuildId());
        workspace.mkdirs();

        boolean result = runner.test(testJob, line -> logs.add(line));

        assertFalse(result);                                                // not a Maven project
        assertTrue(logs.stream().anyMatch(line -> line.contains("Executing command: ./mvnw test")));  // verify test command was attempted logged

        runner.cleanup(testJob);
    }

}