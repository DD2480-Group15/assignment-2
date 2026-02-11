package se.kth.dd2480.group15.api.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.kth.dd2480.group15.api.dto.response.BuildListResponse;
import se.kth.dd2480.group15.api.dto.response.BuildLogResponse;
import se.kth.dd2480.group15.api.dto.response.BuildMetaResponse;
import se.kth.dd2480.group15.services.BuildService;

/**
 * REST controller providing API endpoints for build history, metadata and logs.
 */
@RestController
@RequestMapping("/api/v1/builds")
public class BuildController {

    private final BuildService service;

    /**
     * Constructs a new BuildController instance with the specified build service.
     *
     * @param service the BuildService instance used to handle business logic related to builds
     */
    public BuildController(BuildService service) {
        this.service = service;
    }

    /**
     * Retrieves a list of all build attempts recorded by the CI system.
     * 
     * @return  a {@link BuildListResponse} containing a list of build metadata
     */
    @GetMapping
    public BuildListResponse getBuildHistory() {
        return service.getAllBuilds();
    }

    /**
     * Retrieves specific metadata for a single build.
     * 
     * @param buildId   the unique identifier for the build
     * @return          a {@link BuildMetaResponse} with details like status and commit SHA.
     */
    @GetMapping("/{buildId}")
    public BuildMetaResponse getBuildMetadata(@PathVariable UUID buildId) {
        return service.getBuild(buildId);
    }

    /**
     * Retrieves the console output (logs) for a specific build.
     * 
     * @param buildId   the unique identifier for the build
     * @return          a {@link BuildLogResponse} containing the raw log.
     */
    @GetMapping("/{buildId}/log")
    public BuildLogResponse getBuildLog(@PathVariable UUID buildId) {
        return service.getBuildLog(buildId);
    }
}