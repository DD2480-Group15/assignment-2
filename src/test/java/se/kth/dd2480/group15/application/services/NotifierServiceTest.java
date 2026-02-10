package se.kth.dd2480.group15.application.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class NotifierServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private NotifierService notifierService;
    private String token;

    @BeforeEach
    void setup() throws Exception {
        // Ensure dotenv can find a token during tests, otherwise create a mock one
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            try (FileWriter fw = new FileWriter(".env")) {
                fw.write("GITHUB_TOKEN=dummy_token\n");
            }
        }

        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        notifierService = new NotifierService(restTemplate);
    }

    /**
     * Verifies that {@code notify} returns {@code true} when the information are corrected and the commit do exists
     * <p>
     * Test setup: Create a mock response for the api call to the url, and response HttpStatus.CREATED
     * Expected outcome: return true
     */
    @Test
    void notify_returnsTrue() {
        String owner = "Selinaliu1030";
        String repo = "test_CI";
        String after = "8ddd5caef7130e90a15e2b8e0707d5596654b4b9";
        String state = "success";
        String description = "Build passed";
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/statuses/" + after;
        
        Dotenv dotenv = Dotenv.load();
        token = dotenv.get("GITHUB_TOKEN");

        //create a mock response for the api call
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{}"));

        boolean result = notifierService.notify(owner, repo, after, state, description);
        assertTrue(result);
        server.verify(); // checked the request are made
    }

    /**
     * Verifies that {@code notify} returns {@code false} when the corresponding information points to a non exist commit.
     * <p>
     * Test setup: Create a mock response for the api call to the url, and response HttpStatus.NOT_FOUND along with the error message
     * Expected outcome: return false
     */
    @Test
    void notify_wrongInfo_returnsFalse() {
        String owner = "owner";
        String repo = "repo";
        String after = "after";
        String state = "success";
        String description = "Build failed";
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/statuses/" + after;

        Dotenv dotenv = Dotenv.load();
        token = dotenv.get("GITHUB_TOKEN");

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Not Found\"}"));

        boolean result = notifierService.notify(owner, repo, after, state, description);
        assertFalse(result);
        server.verify();
    }

}
