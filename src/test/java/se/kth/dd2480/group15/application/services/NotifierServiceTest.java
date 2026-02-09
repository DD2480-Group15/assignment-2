package se.kth.dd2480.group15.application.services;

import org.junit.jupiter.api.Test;
import se.kth.dd2480.group15.application.services.NotifierService;
import static org.junit.jupiter.api.Assertions.*;

public class NotifierServiceTest {

    /**
     * Verifies that {@code notify} returns {@code true} when the information are corrected and the commit do exists
     * <p>
     * Test setup: Set up owener, repo, after to connect to a real actual commit.
     * Expected outcome: return true
     */
    @Test
    void notify_returnsTrue() {
        NotifierService notifierService = new NotifierService();
        String owner = "Selinaliu1030";
        String repo = "test_CI";
        String after = "8ddd5caef7130e90a15e2b8e0707d5596654b4b9";
        String state = "success";
        String description = "Build passed";

        boolean result = notifierService.notify(owner, repo, after, state, description);
        assertTrue(result);
    }

    /**
     * Verifies that {@code notify} returns {@code false} when the corresponding information points to a non exist commit.
     * <p>
     * Test setup: Set up owener, repo, after to be points to a non exist commit.
     * Expected outcome: return false
     */
    @Test
    void notify_wrongInfo_returnsFalse() {
        NotifierService notifierService = new NotifierService();
        String owner = "owner";
        String repo = "repo";
        String after = "after";
        String state = "success";
        String description = "Build failed";

        boolean result = notifierService.notify(owner, repo, after, state, description);
        assertFalse(result);
    }

}
