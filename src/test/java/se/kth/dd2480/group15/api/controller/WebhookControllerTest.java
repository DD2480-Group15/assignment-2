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

    @Test
    void WebhookController_returns200OK() throws Exception {
        String jsonPayload = """
            {
                "ref": "refs/heads/main",
                "after": "1234567890abcdef",
                "repository": {
                    "name": "assignment-2"
                }
            }
            """;

        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("CI job started for 1234567890abcdef"));
    }
}