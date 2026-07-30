package model.season.miniGameSeason;

import model.level.Level;
import model.season.Season;

import java.util.List;

public class WallnutsSeason extends Season {
    public WallnutsSeason(List<Level> levels) {
        super("Wallnutbowling", levels, 0);
    }

    @Override
    public boolean sunFallsFromSky() { return false; }

}
