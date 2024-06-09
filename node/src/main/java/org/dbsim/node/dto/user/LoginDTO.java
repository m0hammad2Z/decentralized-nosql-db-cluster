package org.dbsim.node.dto.user;

public class LoginDTO {
    private String adminUsername;
    private String adminPassword;
    private String username;
    private String password;

    public LoginDTO() {
    }
    public LoginDTO(String adminUsername, String adminPassword, String username, String password) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.username = username;
        this.password = password;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
