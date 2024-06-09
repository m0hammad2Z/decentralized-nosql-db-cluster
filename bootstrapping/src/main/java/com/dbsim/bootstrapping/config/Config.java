package com.dbsim.bootstrapping.config;

import com.dbsim.bootstrapping.util.GlobalVar;
import org.dbsim.broadcasting.api.Communication;
import org.dbsim.broadcasting.core.Broadcaster;
import org.dbsim.broadcasting.core.ListenerManager;
import org.dbsim.broadcasting.transport.SocketCommunication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {


    @Bean
    @DependsOn({"broadcaster"})
    public Communication communication() {
        int port = GlobalVar.BROADCASTING_LISTENER_PORT;
        return new SocketCommunication(port, "com.dbsim.bootstrapping", 1);
    }

    @Bean
    public Broadcaster broadcaster() {
        return new Broadcaster(1);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
