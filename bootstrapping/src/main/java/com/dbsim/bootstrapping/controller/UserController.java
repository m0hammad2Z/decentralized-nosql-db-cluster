package com.dbsim.bootstrapping.controller;


import com.dbsim.bootstrapping.dto.JoinDTO;
import com.dbsim.bootstrapping.model.message.ApiResponse;
import com.dbsim.bootstrapping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody JoinDTO joinDTO) {
        return ResponseEntity.ok(userService.joinCluster(joinDTO));
    }
}
