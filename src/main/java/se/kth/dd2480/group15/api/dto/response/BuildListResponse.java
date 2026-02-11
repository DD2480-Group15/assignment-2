package se.kth.dd2480.group15.api.dto.response;
import java.util.List;

/**
 * DTO providing a list of build objects {"builds": [...]}
 */
public class BuildListResponse {
    public List<BuildMetaResponse> builds;

    public BuildListResponse() {} // for Jackson to deserialize JSON

    public BuildListResponse(List<BuildMetaResponse> builds) {
        this.builds = builds;
    }
}