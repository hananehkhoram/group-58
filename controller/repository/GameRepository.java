package controller.repository;

import model.GameContext;

import java.util.List;

public class GameRepository {
    public void saveGame(GameContext ctx, String fileNAme) {
        //save the whole game
        // should be called in game loop/ game engine before exit
    }

    public List<String> getAvailableSaves() {
        //list all available games
        return null;
    }
}
