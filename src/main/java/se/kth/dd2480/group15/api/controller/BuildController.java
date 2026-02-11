package se.kth.dd2480.group15.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.kth.dd2480.group15.api.dto.response.BuildListResponse;
import se.kth.dd2480.group15.api.dto.response.BuildLogResponse;
import se.kth.dd2480.group15.api.dto.response.BuildMetaResponse;

/**
 * REST controller providing API endpoints for build history, metadata and logs.
 * 
 * !Currently using mock data, will integrate with frontend (issue #21) before replacing with real data (issue #53)
 */
@RestController
@RequestMapping("/api/v1/builds")
public class BuildController {

    /**
     * Retrieves a list of all build attempts recorded by the CI system.
     * 
     * @return  a {@link BuildListResponse} containing a list of build metadata
     */
    @GetMapping
    public BuildListResponse getBuildHistory() {
        BuildMetaResponse mock = new BuildMetaResponse("1", "af6d2c", "Rasmus", "SUCCESS", "2026-02-07");
        return new BuildListResponse(List.of(mock));    
    }

    /**
     * Retrieves specific metadata for a single build.
     * 
     * @param buildId   the unique identifier for the build
     * @return          a {@link BuildMetaResponse} with details like status and commit SHA.
     */
    @GetMapping("/{buildId}")
    public BuildMetaResponse getBuildMetadata(@PathVariable String buildId) {
        return new BuildMetaResponse(buildId, "af6d2c", "Rasmus", "SUCCESS", "2026-02-07"); 
    }

    /**
     * Retrieves the console output (logs) for a specific build.
     * 
     * @param buildId   the unique identifier for the build
     * @return          a {@link BuildLogResponse} containing the raw log.
     */
    @GetMapping("/{buildId}/log")
    public BuildLogResponse getBuildLog(@PathVariable String buildId) {
        return new BuildLogResponse("Log for build " + buildId);
    }
}