package model.MiniGame.Izambi;

import controller.MenuManager;
import controller.repository.factory.LevelFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.season.Season;
import model.season.miniGameSeason.IzombieSeason;
import view.ConsoleView;

import java.util.List;

public class Izambi {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    public Izambi() {}

    public void startMiniGame() {
        List<Level> bowlingLevels = LevelFactory.buildWallnutsLevels();
        this.currentLevel = bowlingLevels.get(0);

        Season IzombieSeason = new IzombieSeason(bowlingLevels);

        this.ctx = new GameContext(this.currentLevel, IzombieSeason);
        this.gameEngine = new GameEngine(this.ctx, new MenuManager(ctx));

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
            ConsoleView.showMessage("Game engine is null");
        }
    }

}
