package se.kth.dd2480.group15.api.dto.request;

/**
 * DTO class to represent the structure of the JSON payload sent by GitHub for push events.
 * <p>
 * 
 * @param after        the commit id of the pushed commit
 * @param ref          the branch name of the pushed commit
 * @param repository   the repository object of the pushed commit
 */
public record PushRequestDTO (
        String after, //commit id
        String ref, // branch eg. "refs/heads/main 
        Repository repository // nested object
) {

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
     * <p>
     * 
     * @param name         the repository name
     * @param clone_url    the url to clone the repository
     * @param owner        the Owner object which contains information about the owner of the repo
     */
    public static record Repository (
        String name,
        String clone_url,
        Owner owner
    ) {
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

        /**
         * Handle the request from other functions to fetch the owner object, which contains information about the owner of the repo.
         * <p>
         * 
         * @return the owner object
         */
        public Owner getOwner() {return owner;}
    }

    /**
     * Inner class to represent the structure of the owner object
     * <p>
     * 
     * @param name         the repository name
     */
    public static record Owner (
        String name
    ) {
        public String getName() { return name; }
    }
}