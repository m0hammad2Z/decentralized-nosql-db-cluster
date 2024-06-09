package com.dbsim.demo;

import com.dbsim.demo.model.User;
import com.dbsim.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Objects;


@SpringBootApplication
@EnableScheduling
public class App
{
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }


}
