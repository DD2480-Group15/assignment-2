package se.kth.dd2480.group15.application.services;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for notifying GitHub to create a commit status through the GitHub REST API.
 */
@Service
public class NotifierService {
    private String token;
    
    /**
     * This method sends an authenticated POST request to the GitHub REST API:
     * {@code POST /repos/{owner}/{repo}/statuses/{commit_id}}
     * to create commit status for a specific commit.
     * The process is authenticated by github PAT(Personal Access Token) which is stored in .env
     * <p>
     * 
     * @param owner       The owener of the repo whose commit will be creating status on.
     * @param repo        The repository name of the commit.
     * @param after       The commit id
     * @param state       The state that will be created. Choose from : {success, fail, pending}.
     * @param description The description that will be shown for the commit 
     * @return  true if success, false otherwise.
     */
    public boolean notify(String owner, String repo, String after, String state, String description) {
        Dotenv dotenv = Dotenv.load();
        token = dotenv.get("GITHUB_TOKEN");

        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/statuses/" + after;

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("state", state);
        requestBody.put("description", description);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED) 
                return true;
            else 
                return false;
            
        } catch (Exception e) {
            System.out.println("Error updating status: " + e.getMessage());
        }
        return false;
    }
}