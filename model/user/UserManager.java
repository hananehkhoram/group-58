package model.user;

import java.util.List;

public class UserManager {//singelton

    private List<User> users;
    private User currentUser;

    private UserManager() {
    }

    public UserManager getInstance() {
        return null;
    }

    public boolean register() {
        return false;
    }

    public boolean login() {
        return false;
    }

    public void logOut() {
    }

    public User getCurrentUser() {
        return null;
    }

    public void saveToFile() {
    }

    public void loadFromFile() {
    }
}
