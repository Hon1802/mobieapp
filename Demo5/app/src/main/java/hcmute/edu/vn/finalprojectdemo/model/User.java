package hcmute.edu.vn.finalprojectdemo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private int id;
    private String username;
    private String password;
    private String email;
    private ArrayList<Item> history;

    public User(int id, String username, String password, String email, ArrayList<Item> history) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.history = history;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<Item> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Item> history) {
        this.history = history;
    }
}
