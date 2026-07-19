package model.MiniGame.VaseGame;

import controller.repository.factory.LevelFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.season.Season;
import model.season.miniGameSeason.vaseSeason;

import java.util.List;

public class Vasecheccker {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    public Vasecheccker() {}

    public void startMiniGame() {
        List<Level> bowlingLevels = LevelFactory.buildWallnutsLevels();
        this.currentLevel = bowlingLevels.get(0);

        Season vaseSeason = new vaseSeason(bowlingLevels);

        this.ctx = new GameContext(this.currentLevel, vaseSeason);
        this.gameEngine = new GameEngine(this.ctx);

        System.out.print("start\n");

    }

    public GameContext getCtx(){
        return  this.ctx;
    }

    public GameEngine getGameEngine(){
        return this.gameEngine;
    }

    public void advancedTimeCommand(double sec){
        if (this.gameEngine != null) {
            this.gameEngine.update(sec);
        }else {
            System.out.println("Game engine is null");
        }
    }


}
