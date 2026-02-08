package se.kth.dd2480.group15.api.dto.request;

/*
 * DTO class to represent the structure of the JSON payload sent by GitHub for push events.
 */
public class PushRequestDTO {
    private String after; //commit id
    private String ref; // branch eg. "refs/heads/main 
    private Repository repository; // nested object

    /**
     * Handle the request from other functions to fetch the payload {@code after}, which is the commit id, sent by Github
     * <p>
     * 
     * @return the commit id from the payload sent by GitHub when push event is triggered.
     */
    public String getAfter() { return after; }

    /**
     * Handle the request from other functions to fetch the payload {@code ref}, which is the branch name, sent by Github.
     * The refs is formated as "refs/heads/branch_name".
     * <p>
     * 
     * @return the branch name from the payload sent by GitHub when push event is triggered.
     */
    public String getRef() { return ref; }

    /**
     * Handle the request from other functions to fetch the payload {@code repository}, which is the repository object, sent by Github.
     * The repository is a nested object contains the repository name and clone url.
     * <p>
     * 
     * @return the repository object from the payload sent by GitHub when push event is triggered.
     */
    public Repository getRepository() { return repository; }

    /**
     * Inner class to represent the structure of the repository object in the JSON payload sent by GitHub for push events.
     */
    public static class Repository {
        private String name;
        private String clone_url;

        /**
         * Handle the request from other functions to fetch the payload {@code name}, which is the repository name, sent by Github.
         * <p>
         * 
         * @return the repository name under the repository object
         */
        public String getName() { return name; }

        /**
         * Handle the request from other functions to fetch the payload {@code clone_url}, which is the url to clone the repo
         * <p>
         * 
         * @return the clone url under the repository object
         */
        public String getClone_url() { return clone_url; }
    }
}