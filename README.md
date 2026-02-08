# assignment-2
Continuous Integration

## Configure Github Template
- Commit Message Template
```bash
git config commit.template .gitmessage
```

## Running and testing

This project uses the **Maven Wrapper** to ensure a consistent Maven version across environments.  
No local Maven installation is required.

To run the code: 
```bash
./mvnw spring-boot:run
```


### Run tests

```bash
./mvnw test
```
