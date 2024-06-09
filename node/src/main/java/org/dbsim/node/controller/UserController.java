package org.dbsim.node.controller;

import org.dbsim.node.dto.user.LoginDTO;
import org.dbsim.node.dto.user.UserDTO;
import org.dbsim.node.model.message.ApiResponse;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.service.user.UserService;
import org.dbsim.node.util.communication.ForwardRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {
    private UserService userService;
    private ForwardRequestUtil forwardRequestUtil;
    private MainNode mainNode;

    @Autowired
    public UserController(UserService userService, ForwardRequestUtil forwardRequestUtil, MainNode mainNode) {
        this.userService = userService;
        this.forwardRequestUtil = forwardRequestUtil;
        this.mainNode = mainNode;
    }

    @PostMapping("/addUser")
    public ResponseEntity<ApiResponse<UserDTO>> addUser(@RequestBody UserDTO userDTO) {
        int affinityNode = mainNode.getAffinityNodeId(userDTO.getUsername());
        if (affinityNode == -1) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }

        if (affinityNode == mainNode.getId()) {
            UserDTO savedUserDTO = userService.save(userDTO);
            return ResponseEntity.status(201).body(new ApiResponse<>(true, "User created successfully", savedUserDTO));
        }

        return forwardRequestUtil.forward(userDTO, mainNode.getNeighborNode(affinityNode), "/addUser", HttpMethod.POST);

    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<ApiResponse<String>> deleteUser(@RequestBody String username) {
        int affinityNode = mainNode.getAffinityNodeId(username);
        if (affinityNode == -1) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }

        if (affinityNode == mainNode.getId()) {
            userService.delete(username);
            return ResponseEntity.status(200).body(new ApiResponse<>(true, "User deleted successfully", null));
        }

        return forwardRequestUtil.forward(username, mainNode.getNeighborNode(affinityNode), "/deleteUser", HttpMethod.DELETE);
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginDTO loginDTO) {
        Map<String, String> response = userService.login(loginDTO);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, "User logged in successfully", response));
    }

}
