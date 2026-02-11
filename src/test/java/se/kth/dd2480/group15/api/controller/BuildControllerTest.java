package se.kth.dd2480.group15.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.kth.dd2480.group15.api.dto.response.BuildListResponse;
import se.kth.dd2480.group15.api.dto.response.BuildLogResponse;
import se.kth.dd2480.group15.api.dto.response.BuildMetaResponse;
import se.kth.dd2480.group15.domain.Build;
import se.kth.dd2480.group15.domain.BuildSummary;
import se.kth.dd2480.group15.services.BuildService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuildController.class)
class BuildControllerTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuildService buildService;

    /**
     * Verifies that the {@code getBuildHistory} endpoint returns a {@code HTTP 200 OK}
     * response with the correct JSON payload containing the build history.
     * <p>
     * Test setup:
     * - A mock {@link BuildListResponse} is created with a single {@link BuildSummary}.
     * - The {@code buildService.getAllBuilds()} method is mocked to return the mock response.
     * - The expected JSON representation of the mock response is generated.
     * - A GET request is performed on the {@code /api/v1/builds} endpoint using {@link MockMvc}.
     */
    @Test
    void getBuildHistory_whenBuildsExist_returnsOkAndBuildListJson() throws Exception {
        BuildSummary summary = new BuildSummary(
                UUID.randomUUID(),
                "abc123",
                Instant.now()
        );
        BuildListResponse mockResponse = new BuildListResponse(List.of(summary));
        when(buildService.getAllBuilds()).thenReturn(mockResponse);

        String expectedJson = MAPPER.writeValueAsString(mockResponse);
        mockMvc.perform(get("/api/v1/builds"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    /**
     * Verifies that the {@code getBuildMetadata} endpoint returns a {@code HTTP 200 OK}
     * response along with the correct JSON representation of the build metadata.
     * <p>
     * Test setup:
     * - A {@link Build} object is created to simulate a specific build process.
     * - A {@link BuildMetaResponse} object is constructed using the details of the mocked build.
     * - The {@code buildService.getBuild()} method is mocked to return the constructed
     *   {@link BuildMetaResponse}.
     * - The expected JSON representation of the response is generated.
     * - A GET request is made to the {@code /api/v1/builds/{buildId}
     */
    @Test
    void getBuildMetadata_whenBuildExists_returnsOkAndBuildMetaJson() throws Exception {
        Build build = Build.newBuild("abc123", "url", "owner", "repo");
        BuildMetaResponse mockResponse = new BuildMetaResponse(
                build.getBuildId(),
                build.getCommitSha(),
                build.getRepoOwner(),
                build.getStatus(),
                build.getCreatedAt()
        );
        when(buildService.getBuild(build.getBuildId())).thenReturn(mockResponse);

        String expectedJson = MAPPER.writeValueAsString(mockResponse);
        mockMvc.perform(get("/api/v1/builds/" + build.getBuildId()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    /**
     * Verifies that the {@code /api/v1/builds/{buildId}/log} endpoint returns a {@code
     * HTTP 200 OK} response with the correct JSON payload containing the build log.
     * <p>
     * Test setup:
     * - A mock build identifier is created as well as a sample log string.
     * - A {@link BuildLogResponse} object containing the sample log string is mocked
     *   as the return value of {@code buildService.getBuildLog(UUID)}.
     * - The expected JSON representation of the mocked response is generated.
     * - A GET request is made to the {@code /api/v1/builds/{buildId}/log} endpoint.
     */
    @Test
    void getBuildLog_whenLogExists_returnsOkAndBuildLogJson() throws Exception {
        UUID buildId = UUID.randomUUID();
        String logContent = "Sample build log content";
        BuildLogResponse mockResponse = new BuildLogResponse(logContent);
        when(buildService.getBuildLog(buildId)).thenReturn(mockResponse);

        String expectedJson = MAPPER.writeValueAsString(mockResponse);
        mockMvc.perform(get("/api/v1/builds/" + buildId + "/log"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }
}