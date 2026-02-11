package se.kth.dd2480.group15.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Build domain model
 * Verifies that the build state and the different transitions and timestamps are handled correctly
 */
class BuildTest {

    /**
     * Verifies that newBuild initializes a build with the correct default values
     * 
     * Input: A specific commit SHA, repository URL, repository owner and repository name
     * Expected outcome: Status is QUEUED, timestamps are initialized correctly 
     * and the provided SHA, URL, repository owner and repository name should be stored
     * 
     */
    @Test
    void newBuildShouldHaveCorrectDefaults() {
        String sha = "testtest123";
        String url = "https://github.com/user/repo";
        String owner = "owner1";
        String name = "name2";
        
        Build build = Build.newBuild(sha, url, owner, name);

        assertNotNull(build.getBuildId());
        assertEquals(sha, build.getCommitSha());
        assertEquals(url, build.getRepoUrl());
        assertEquals(owner, build.getRepoOwner());
        assertEquals(name, build.getRepoName());
        assertEquals(Build.Status.QUEUED, build.getStatus());
        assertNotNull(build.getCreatedAt());
    }

    /**
     * Verifies that {@code startBuild} updates the build status to RUNNING 
     * and records the start timestamp
     * 
     * Input: A new build in QUEUED state
     * Expected outcome: Status changes to RUNNING and {@code startedAt} should no longer be null
     * 
     */
    @Test
    void startBuildShouldUpdateStatusAndTimestamp() {
        Build build = Build.newBuild("sha", "url", "owner", "name");
        build.startBuild();
        
        assertEquals(Build.Status.RUNNING, build.getStatus());
        assertNotNull(build.getStartedAt());
    }

    /**
     * Verifies that {@code finishBuild} updates the build status to SUCCESS
     * and records the finish timestamp
     * 
     * Input: A build that has been started (status RUNNING)
     * Expected outcome: Status changes to SUCCESS and {@code finishedAt} should be recorded
     * 
     */
    @Test
    void finishBuildShouldSetSuccessStatus() {
        Build build = Build.newBuild("sha", "url", "owner", "name");
        build.startBuild();
        build.finishBuild();
        
        assertEquals(Build.Status.SUCCESS, build.getStatus());
        assertNotNull(build.getFinishedAt());
    }

    /**
     * Verifies that {@code failBuild} updates the build status to FAILED
     * and records the finish timestamp
     * 
     * Input: A build that has been started (status is RUNNING)
     * Expected outcome: Status changes to FAILED and {@code finishedAt} is recorded
     * 
     */
    @Test
    void failBuildShouldSetFailedStatus() {
        Build build = Build.newBuild("sha", "url", "owner", "name");
        build.startBuild();
        build.failBuild();
        
        assertEquals(Build.Status.FAILED, build.getStatus());
        assertNotNull(build.getFinishedAt());
    }
}