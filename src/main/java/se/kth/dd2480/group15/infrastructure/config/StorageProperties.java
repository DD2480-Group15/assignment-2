package se.kth.dd2480.group15.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for storage-related settings in the CI system.
 * The properties are specified in the application properties file
 */
@ConfigurationProperties(prefix = "ci.storage")
public record StorageProperties(String buildsRoot) { }
