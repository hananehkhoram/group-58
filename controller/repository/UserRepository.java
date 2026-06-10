package controller.repository;

import model.user.User;

import java.util.HashMap;
import java.util.Map;

// load and save data
public class UserRepository implements AssetRepository<User> {
    private final Map<String, User> userMap = new HashMap<>();

    @Override
    public void load(String filePath) {

    }

    @Override
    public User get(String id) {
        return null;
    }
}
