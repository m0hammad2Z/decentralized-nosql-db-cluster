package org.dbsim.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.node.model.message.ApiResponse;
import org.dbsim.node.model.node.NeighborNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;

@EnableAsync
@SpringBootApplication
@Configuration
@EnableScheduling
public class App
{
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
