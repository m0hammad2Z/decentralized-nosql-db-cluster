package com.dbsim.demo.service;

import com.dbsim.demo.config.db.DatabaseConnection;
import com.dbsim.demo.model.ApiResponse;
import com.dbsim.demo.model.Email;
import com.dbsim.demo.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private DatabaseConnection databaseConnection;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUser(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("password", passwordEncoder.encode(user.getPassword()));
        userData.put("phone", user.getPhone());

        // Check if the user already exists
        ApiResponse<List<Map<String, Object>>> userExistResponse1 = databaseConnection.getDocumentsByProperty("users", "email", user.getEmail());
        if (userExistResponse1.getData() != null && !userExistResponse1.getData().isEmpty()) {
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }

        // Check if the user already exists
        ApiResponse<List<Map<String, Object>>> userExistResponse2 = databaseConnection.getDocumentsByProperty("users", "username", user.getUsername());
        if (userExistResponse2.getData() != null && !userExistResponse2.getData().isEmpty()) {
            throw new RuntimeException("User with username " + user.getUsername() + " already exists");
        }


        ApiResponse response = databaseConnection.createDocument("users", userData);
        if (!response.getSuccess()) {
            throw new RuntimeException("Failed to create user: " + response.getMessage());
        }
    }

    public User getUserByUsername(String username)  {
        ApiResponse<List<Map<String, Object>>> response = databaseConnection.getDocumentsByProperty("users", "username", username);
        List<Map<String, Object>> documents = response.getData();
        if (documents == null || documents.isEmpty() || documents.get(0) == null|| documents.get(0).get("document") == null) {
            throw new RuntimeException("User not found");
        }

        // Get the first document in the response
        Map<String, Object> documentMap = (Map<String, Object>) documents.get(0).get("document");

        User user = new User();
        user.setUsername((String) documentMap.get("username"));
        user.setEmail((String) documentMap.get("email"));
        user.setPassword((String) documentMap.get("password"));
        user.setPhone((String) documentMap.get("phone"));

        return user;
    }

    public User getUserByEmail(String email) {
        ApiResponse<List<Map<String, Object>>> response = databaseConnection.getDocumentsByProperty("users", "email", email);
        List<Map<String, Object>> documents = response.getData();
        if (documents == null || documents.isEmpty() || documents.get(0) == null || documents.get(0).get("document") == null) {
            throw new RuntimeException("User not found");
        }

        // Get the first document in the response
        Map<String, Object> documentMap = (Map<String, Object>) documents.get(0).get("document");

        User user = new User();
        user.setUsername((String) documentMap.get("username"));
        user.setEmail((String) documentMap.get("email"));
        user.setPassword((String) documentMap.get("password"));
        user.setPhone((String) documentMap.get("phone"));

        return user;
    }


    public User updateUser(User user) throws JsonProcessingException {
        String documentId = databaseConnection.getDocumentId("users", "username", user.getUsername());
        int version = databaseConnection.getDocumentVersion("users", "username", user.getUsername());
        //Get ole user
        User oldUser = getUserByUsername(user.getUsername());


        // Check if the email and username same as the old user
        if (user.getEmail() != null && !user.getEmail().equals(oldUser.getEmail())) {
            ApiResponse<List<Map<String, Object>>> userExistResponse1 = databaseConnection.getDocumentsByProperty("users", "email", user.getEmail());
            if (userExistResponse1.getData() != null && !userExistResponse1.getData().isEmpty()) {
                throw new RuntimeException("User with email " + user.getEmail() + " already exists");
            }
        }

        if (user.getUsername() != null && !user.getUsername().equals(oldUser.getUsername())) {
            ApiResponse<List<Map<String, Object>>> userExistResponse2 = databaseConnection.getDocumentsByProperty("users", "username", user.getUsername());
            if (userExistResponse2.getData() != null && !userExistResponse2.getData().isEmpty()) {
                throw new RuntimeException("User with username " + user.getUsername() + " already exists");
            }
        }

        // Update the user
        String phone = user.getPhone() == null ? oldUser.getPhone() : user.getPhone();
        String password = user.getPassword() == null || user.getPassword().isEmpty() || passwordEncoder.matches(user.getPassword(), oldUser.getPassword()) ? oldUser.getPassword() : passwordEncoder.encode(user.getPassword());

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("password", password);
        userData.put("phone", phone);

        ApiResponse<Map<String, Object>> response = databaseConnection.updateDocument("users", documentId, userData, version);

        if (response.getSuccess()) {
            return user;
        } else {
            throw new RuntimeException("Failed to update user: " + response.getMessage());
        }
    }
    public void deleteUser(String username) {
        String documentId = databaseConnection.getDocumentId("users", "username", username);
        ApiResponse response = databaseConnection.deleteDocument("users", documentId);

        if (response.getSuccess()) {
            throw new RuntimeException("Failed to delete user: " + response.getMessage());
        }
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}