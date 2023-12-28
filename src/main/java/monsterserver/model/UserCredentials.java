package monsterserver.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class UserCredentials {
    @JsonAlias({"Username"})
    private String username;
    @JsonAlias({"Password"})
    private String password;

    // Jackson needs the default constructor
    public UserCredentials() {}

    public UserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
