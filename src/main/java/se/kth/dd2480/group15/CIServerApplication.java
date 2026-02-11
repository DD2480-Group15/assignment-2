package se.kth.dd2480.group15;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import se.kth.dd2480.group15.infrastructure.config.StorageProperties;

@EnableConfigurationProperties(StorageProperties.class)
@SpringBootApplication
public class CIServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CIServerApplication.class, args);
    }
}
