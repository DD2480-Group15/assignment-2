package se.kth.dd2480.group15.api.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.kth.dd2480.group15.api.dto.request.PushRequestDTO;

@RestController
public class WebhookController {

    /**
     * Handle the webhook POST response from GitHub when push event is triggered 
     * and fetch the payload content as JSON to extract the relevant information for the CI job.
     * <p>
     * 
     * @param payload       the JSON payload which is configured as PushRequestDTO sent by GitHub containing information about the event.
     * @return 200 OK with a message if the payload is processed successfully, otherwise returns an error message.
     */
    @PostMapping("/webhook")
    public String handleWebhook(@RequestBody PushRequestDTO payload) {
        try {
            String repo = payload.getRepository().getName();
            String branch = payload.getRef();
            String commit = payload.getAfter();

            System.out.println("Received build request for:");
            System.out.println("Repo: " + repo + " | Branch: " + branch + " | Commit: " + commit);

            return "CI job started for " + commit; // return 200 OK with message
        } catch (Exception e) {
            return "Error processing webhook: " + e.getMessage();
        }
    }
        
}

