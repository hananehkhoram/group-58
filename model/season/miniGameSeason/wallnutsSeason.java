package model.season.miniGameSeason;

import model.level.Level;
import model.season.Season;

import java.util.List;

public class wallnutsSeason extends Season {
    public wallnutsSeason(List<Level> levels) {
        super("Wallnutbowling", levels, 0);
    }

    @Override
    public boolean sunFallsFromSky() { return false; }

}
