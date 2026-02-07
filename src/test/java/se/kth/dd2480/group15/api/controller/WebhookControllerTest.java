package se.kth.dd2480.group15.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import se.kth.dd2480.group15.api.controller.WebhookController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Verifies that {@code handleWebhook} returns {@code HTTP 200 OK} when the
     * payload is correctly formatted and contains valid data. 
     * This test simulates a typical webhook payload using mockMvc to send a POST request to the /webhook endpoint, 
     * which ensures the controller can parse the JSON and respond appropriately.
     * <p>
     * Test setup: A JSON payload is created with a repository name, branch reference, and commit hash.
     * The payload is sent as a POST request to the /webhook endpoint using mockMvc.
     * 
     * Expected outcome: The controller should return a 200 OK status with a message 
     * indicating that the CI job has started for the given commit hash.
     * </p>
     */
    @Test
    void handleWebhook_returns200OK() throws Exception {
        String jsonPayload = """
            {
                "ref": "refs/heads/main",
                "after": "1234567890abcdef",
                "repository": {
                    "name": "assignment-2"
                }
            }
            """;

        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("CI job started for 1234567890abcdef"));
    }

    /**
     * Verifies that {@code handleWebhook} returns {@code HTTP 400 Bad Request} when the
     * payload is incorrectly formatted. 
     * This test simulates a malformed webhook payload using mockMvc to send a POST request to the /webhook endpoint, 
     * which ensures the controller can handle invalid JSON and respond appropriately.
     * <p>
     * Test setup: A JSON payload is created with a repository name, branch reference, and commit hash.
     * The payload is sent as a POST request to the /webhook endpoint using mockMvc.
     * 
     * Expected outcome: The controller should return a 400 Bad Request status.
     * </p>
     */
    @Test
    void handleWebhook_payloadWrongFormat_returnsBadRequest() throws Exception {
        String malformedJson = "this is not json";

        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}