package se.kth.dd2480.group15.api.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class WebhookController {
    @PostMapping("/webhook")
    //parameter will change to DTO once we have defined it
    public String handleWebhook(@RequestBody JsonNode payload) {
        try {
            String repo = payload.path("repository").path("name").asText();
            String branch = payload.path("ref").asText();
            String commit = payload.path("after").asText();

            System.out.println("Received build request for:");
            System.out.println("Repo: " + repo + " | Branch: " + branch + " | Commit: " + commit);

            return "CI job started for " + commit; // return 200 OK with message
        } catch (Exception e) {
            return "Error processing webhook: " + e.getMessage();
        }
    }
        
}

