package se.kth.dd2480.group15.api.dto.response;

import se.kth.dd2480.group15.domain.BuildSummary;

import java.util.List;

/**
 * DTO providing a list of build objects {"builds": [...]}
 * 
 * @param builds    the list of build metadata objects
 */
public record BuildListResponse(List<BuildSummary> builds) { }
