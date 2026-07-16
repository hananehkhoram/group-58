package model.MiniGame.Beghouled;

import controller.repository.factory.LevelFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.season.Season;
import model.season.miniGameSeason.beghouledSeason;

import java.util.List;

public class Beghouled {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    public Beghouled() {}

    public void startMiniGame() {
        List<Level> bowlingLevels = LevelFactory.buildWallnutsLevels();
        this.currentLevel = bowlingLevels.get(0);

        Season beghouledSeason = new beghouledSeason(bowlingLevels);

        this.ctx = new GameContext(this.currentLevel, beghouledSeason);
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
