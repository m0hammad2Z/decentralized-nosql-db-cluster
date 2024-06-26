package org.dbsim.node.dto.user;

import javax.validation.constraints.NotBlank;

public class UserDTO {

    private String username;

    private String password;

    private String role;

    public UserDTO() {
    }

    public UserDTO(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
