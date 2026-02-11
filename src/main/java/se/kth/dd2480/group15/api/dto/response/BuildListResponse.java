package se.kth.dd2480.group15.api.dto.response;
import java.util.List;

/**
 * DTO providing a list of build objects {"builds": [...]}
 * 
 * @param builds    the list of build metadata objects
 */
public record BuildListResponse(
    List<BuildMetaResponse> builds
) {}