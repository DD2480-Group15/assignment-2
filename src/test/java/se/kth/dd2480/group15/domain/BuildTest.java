package se.kth.dd2480.group15.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BuildTest {

    @Test
    void newBuildShouldHaveCorrectDefaults() {
        String sha = "testtest123";
        String url = "https://github.com/user/repo";
        
        Build build = Build.newBuild(sha, url);

        assertNotNull(build.getBuildId());
        assertEquals(sha, build.getCommitSha());
        assertEquals(url, build.getRepoUrl());
        assertEquals(Build.Status.QUEUED, build.getStatus());
        assertNotNull(build.getCreatedAt());
    }

    @Test
    void startBuildShouldUpdateStatusAndTimestamp() {
        Build build = Build.newBuild("sha", "url");
        build.startBuild();
        
        assertEquals(Build.Status.RUNNING, build.getStatus());
        assertNotNull(build.getStartedAt());
    }

    @Test
    void finishBuildShouldSetSuccessStatus() {
        Build build = Build.newBuild("sha", "url");
        build.startBuild();
        build.finishBuild();
        
        assertEquals(Build.Status.SUCCESS, build.getStatus());
        assertNotNull(build.getFinishedAt());
    }

    @Test
    void failBuildShouldSetFailedStatus() {
        Build build = Build.newBuild("sha", "url");
        build.startBuild();
        build.failBuild();
        
        assertEquals(Build.Status.FAILED, build.getStatus());
        assertNotNull(build.getFinishedAt());
    }
}