package se.kth.dd2480.group15.api.dto.response;

/**
 * DTO representing the raw log output of a build.
 *
 * @param logContent  the text content from the standard build output
 */
public record BuildLogResponse(String logContent) { }
