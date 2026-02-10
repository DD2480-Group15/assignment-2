package se.kth.dd2480.group15.api.dto.response;

/**
 * DTO representing the raw log output of a build.
 */
public class BuildLogResponse {
    public String logContent;

    public BuildLogResponse() {} // for Jackson to deserialize JSON

    /** 
     * @param logcontent  the text content from the standard build output
     */
    public BuildLogResponse(String logContent) {
        this.logContent = logContent;
    }
}