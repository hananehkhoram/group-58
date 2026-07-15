package model.MiniGame.WallnutsGame;

import controller.repository.factory.LevelFactory;
import model.GameContext;
import model.level.Level;
import model.plants.Plant;
import model.season.Season;
import model.season.miniGameSeason.wallnutsSeason;

import java.util.ArrayList;
import java.util.List;

public class WallnutBowlingGame {
    private Level currentLevel;

    public void WallnutBowlingGame(){
    }

    public void start(){

        List<Level> bowlingLevels = LevelFactory.buildWallnutsLevels();
        Level firstLevel = bowlingLevels.get(0);

        Season bowlingSeason = new wallnutsSeason(bowlingLevels);
        GameContext ctx = new GameContext(firstLevel, bowlingSeason);

    }

}
