package controller.repository;

import model.GameContext;
import model.user.User;
import model.user.UserManager;

import java.util.List;

public class GameRepository {
    public void saveGame(GameContext ctx, String fileName) {
        //save the whole game
        UserManager.getInstance().saveToFile();
        // should be called in game loop/ game engine before exit
        //unnecessary
    }

    public List<User> getAvailableSaves() {
        //list all available games
        UserManager.getInstance().loadFromFile();
        //unnecessary
        return null;
    }
}
