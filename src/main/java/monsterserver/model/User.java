package monsterserver.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class User {
    @JsonAlias({"User_id"})
    private Integer id;
    @JsonAlias({"Username"})
    private String username;
    @JsonAlias({"Password"})
    private String password;

    // Jackson needs the default constructor
    public User() {}

    public User(Integer id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
