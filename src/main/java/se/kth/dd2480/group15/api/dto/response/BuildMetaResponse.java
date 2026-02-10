package se.kth.dd2480.group15.api.dto.response;

/**
 * DTO representing the metadata of a single build in the history list.
 */
public class BuildMetaResponse {
    
    public String buildId;
    public String commitSha;
    public String owner;
    public String status;
    public String createdAt;

    public BuildMetaResponse() {} // for Jackson to deserialize JSON

    /** 
     * @param buildId   the unique session ID for a specific build
     * @param commitSha the 40-character hex hash identifying the git commit
     * @param owner     the GitHub username of the person who pushed the code
     * @param status    the final outcome of the build (SUCCESS, FAILURE, ERROR)
     * @param createdAt the timestamp of when the build was triggered
     */
    public BuildMetaResponse(String buildId, String commitSha, String owner, String status, String createdAt) {
        this.buildId = buildId;
        this.commitSha = commitSha;
        this.owner = owner;
        this.status = status;
        this.createdAt = createdAt;
    }
}