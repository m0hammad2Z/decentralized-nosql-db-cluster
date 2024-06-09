package org.dbsim.node.repository;

import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.enums.DBExceptionErrorType;
import org.dbsim.node.model.user.User;
import org.dbsim.node.util.persistence.FileSystem;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private FileSystem fileSystem;

    @Override
    public User findByUsername(String username) {
        String userPath = getUserPath(username);

        // Check if the user exists
        if (!fileSystem.doesFileExist(userPath, username + ".json")) {
            throw new DBOperationException("User does not exist", DBExceptionErrorType.NOT_FOUND);
        }

        // Read the user file
        Optional<String> userString = fileSystem.getFileContent(userPath, username + ".json");
        if (userString.isEmpty()) {
            throw new DBOperationException("User read failed", DBExceptionErrorType.INTERNAL);
        }

        JSONObject userJson = new JSONObject(userString.get());
        String password = userJson.getString("password");
        String role = userJson.getString("role");
        return new User(username, password, role);

    }

    @Override
    public User save(User user) {
        String userPath = getUserPath(user.getUsername());

        // check if the user directory exists
        if(!fileSystem.doesDirectoryExist("users"))
            fileSystem.createDirectory("users");

        // check if the user already exists
        if(fileSystem.doesFileExist(userPath, user.getUsername() + ".json"))
            throw new DBOperationException("User already exists", DBExceptionErrorType.RESOURCE_ALREADY_EXISTS);

        JSONObject userJson = new JSONObject();
        userJson.put("username", user.getUsername());
        userJson.put("password", user.getPassword());
        userJson.put("role", user.getRole());

        // create the file
        if(!fileSystem.createFile(userPath, user.getUsername() + ".json", userJson.toString()))
            throw new DBOperationException("User creation failed", DBExceptionErrorType.INTERNAL);

        return user;
    }

    @Override
    public void delete(String username) {
        String userPath = getUserPath(username);

        // check if the user exists
        if(!fileSystem.doesFileExist(userPath, username + ".json"))
            throw new DBOperationException("User does not exist", DBExceptionErrorType.NOT_FOUND);

        // delete the file
        if(!fileSystem.deleteFile(userPath, username + ".json"))
            throw new DBOperationException("User deletion failed", DBExceptionErrorType.INTERNAL);
    }

    private String getUserPath(String username) {
        return "users/";
    }
}
