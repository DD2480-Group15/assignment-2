# Assignment-2: Continuous Integration

This assignment is part of the course DD2480 Software Engineering Fundamentals and is an implementation of a small continuous integration CI server, which contains the core features of continuous integration: compilation, testing, and notification.

## Description
The CI server provides three core features: compilation, testing, and notification.
For compilation, the CI server supports building the project source code. The compilation process is triggered by a webhook event. Upon receiving the webhook, the CI server compiles the specific branch where the change occurred, as indicated in the HTTP payload.
For testing, the CI server executes the project’s automated test suite to verify the correctness of the changes.
For notification, the CI server reports the CI results back to GitHub. Based on the outcome, GitHub updates the commit status accordingly, allowing developers to see whether the changes passed or failed the CI process.
### Compilation

#### Implementation
Compilation is managed by the ProcessRunner service, which executes the Maven Wrapper command within the specific build’s workspace. The system uses a ProcessBuilder to execute the compilation and monitors for the exit code: an exit code of 0 equals success, while any other value indicates that the compilation failed.
#### Unit Test
The compilation flow is verified by testing the ProcessRunner’s ability to handle process execution and log the results. We verify that the system identifies failure (e.g. missing mvnw file) and ensures that the Maven commands are properly constructed and passed to the OS. 
### Testing

#### Implementation
Once compilation is successful the CI server moves on to execute the testing phase using the `./mvnw test` command. The ProcessRunner captures the standard output and error streams, allowing the test results and potential stack traces to be streamed to the build logs via a `Consumer<String>`.
#### Unit Test
The testing functionality is verified via integration tests in `ProcessRunnerTest`. The tests perform a real clone of a repository and execute the `test()` method. We use assertions to verify commands are working properly: by streaming the process output to a list. We also assert that logs contain the specific execution string `mvnw test`. This confirms correct initiation and logging pipeline is functional.

### Notification

#### Implementation
Notification feature is implemented as a springboot service, which will send out an authenticated POST request to the GitHub REST API: `https://api.github.com/repos/{owner}/{repo}/statuses/{commit_id}`  to create the commit status when the function is being called. The process is being authenticated by Github Personal Access Token.

#### Unit Test
The notification service is tested using MockRestServiceServer. It will create mock responses when the Github rest api is being called then check if the notifier function can run the whole process successfully.

### Build List URL:

The build history can be accessed through this URL:
https://madalynn-vitiliginous-temptingly.ngrok-free.dev/

## Dependencies

This project uses the **Maven Wrapper** to ensure a consistent Maven version across environments. 
No local Maven installation is required.

The maven wrapper is OS-dependent
- For **Windows**: mvnw.cmd
- For **Mac/Linux**: mvnw

## How to run the program
### .env file
Add GITHUB_TOKEN= ghp_XXXXX (your own github PAT) to .env file
### Run code

#### Mac/Linux

```bash
./mvnw spring-boot:run
```

#### Windows

```bash
mvnw.cmd spring-boot:run
```

### Compile

#### Mac/Linux
```bash
./mvnw compile
```

#### Windows
```bash
mvnw.cmd compile
```

### Run tests

#### Mac/Linux
```bash
./mvnw test
```

#### Windows
```bash
mvnw.cmd test
```



## Statement of contributions

#### Rasmus Sjöberg (rassjo@kth.se):

* Setup GitHub Actions
* Implemented BuildRepository and BuildService
* Corresponding documentation and tests
* Code reviews + merging pull requests

#### Emma Tisell (etisell@kth.se):

* Implemented CIService and corresponding tests
* Corresponding documentation (JavaDoc)
* Code reviews + merging pull requests

#### Chih-Yun Liu (cyliu4@kth.se):

* Implemented NotifierService, and WebhookController and corresponding unit tests
* Implemented PushRequestDTO
* Corresponding documentation (JavaDoc)
* Code reviews + merging pull requests

#### Filippa Ciuk Olsson (fico@kth.se):

* Implemented BuildController and DTOs handling stored build information
* Connected these to a frontend web application
* Corresponding documentation
* Code Reviews + merging pull requests

#### Jacob Friedrich (jafr@kth.se):

* Implemented ProcessRunner and corresponding tests
* Corresponding documentation (JavaDoc)
* Code reviews + merging pull requests

## Ways-of-working

We have established a solid way of working and as time has gone on and we have worked on two 
assignments now we are adapting the ways quite naturally, so we deem that we have moved all 
the way from “In use” to fulfilling most of the requirements of the “Working well” stage. What 
we need to work on for next time is to continually fine-tune our practices and adapt it to the 
next assignment in order to fully show that we are a well-integrated team. For, we 
have been using similar practices for both assignments, but next time we might want to see 
if we can adapt our tools even further for the specific requirement of the next assignment.


