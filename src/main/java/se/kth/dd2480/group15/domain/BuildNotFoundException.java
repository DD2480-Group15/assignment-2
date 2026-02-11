package se.kth.dd2480.group15.domain;

import java.util.UUID;

/**
 * Thrown when a build with the specified identifier cannot be found.
 */
public class BuildNotFoundException extends RuntimeException {

    private final UUID buildId;

    public BuildNotFoundException(UUID buildId) {
        super("Build not found: " + buildId);
        this.buildId = buildId;
    }

    public UUID getBuildId() {
        return buildId;
    }
}
