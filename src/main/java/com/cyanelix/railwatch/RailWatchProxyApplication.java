package com.cyanelix.railwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RailWatchProxyApplication {
    public RailWatchProxyApplication() {
        // Default constructor required by SpringBoot.
    }

    public static void main(String[] args) {
        SpringApplication.run(RailWatchProxyApplication.class, args);
    }
}
