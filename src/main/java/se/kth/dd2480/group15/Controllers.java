package se.kth.dd2480.group15;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class Controllers {
    @PostMapping("/")
    public String handleWebhook(@RequestBody JsonNode payload) {
        try {
            String repo = payload.path("repository").path("name").asText();
            String branch = payload.path("ref").asText();
            String commit = payload.path("after").asText();

            System.out.println("Received build request for:");
            System.out.println("Repo: " + repo + " | Branch: " + branch + " | Commit: " + commit);

            return "CI job started for " + commit;
        } catch (Exception e) {
            return "Error processing webhook: " + e.getMessage();
        }
    }
        
}

