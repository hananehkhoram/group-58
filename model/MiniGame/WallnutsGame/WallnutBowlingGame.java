package model.MiniGame.WallnutsGame;

import controller.MenuManager;
import controller.repository.factory.LevelFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.plants.Plant;
import model.season.Season;
import model.season.miniGameSeason.wallnutsSeason;
import view.ConsoleView;

import java.util.ArrayList;
import java.util.List;

public class WallnutBowlingGame {
    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    public WallnutBowlingGame() {
    }

    public void start() {

        List<Level> bowlingLevels = LevelFactory.buildWallnutsLevels();
        this.currentLevel = bowlingLevels.get(0);

        Season bowlingSeason = new wallnutsSeason(bowlingLevels);

        this.ctx = new GameContext(this.currentLevel, bowlingSeason);
        this.gameEngine = new GameEngine(this.ctx, new MenuManager(ctx));

        ConsoleView.showMessage("start\n");
    }

    // ---- Getter ----

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
            ConsoleView.showMessage("Game engine is null");
        }
    }



}
