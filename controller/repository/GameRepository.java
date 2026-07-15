package controller.repository;

import model.GameContext;
import model.user.User;
import model.user.UserManager;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Map;

public class GameRepository {
    public void saveGame(GameContext ctx, String fileName) {
        //save the whole game
        UserManager.getInstance().saveToFile();
        DataManager.getInstance().saveUser();
        // should be called in game loop/ game engine before exit
        //unnecessary
    }

    public Map<String ,User> getAvailableSaves() {
        //list all available games
        UserManager.getInstance().loadFromFile();
        DataManager.getInstance().loadUser();

        //unnecessary
        return DataManager.getInstance().users.getUserMap();
    }
}
