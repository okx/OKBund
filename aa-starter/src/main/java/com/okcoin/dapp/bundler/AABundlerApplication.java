package com.okcoin.dapp.bundler;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AABundlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AABundlerApplication.class, args);
    }

}
