package com.workshop.model.season.miniGameSeason;

import com.workshop.model.level.Level;
import com.workshop.model.season.Season;

import java.util.List;

public class WallnutsSeason extends Season {
    public WallnutsSeason(List<Level> levels) {
        super("Wallnut Bowling", levels, 0);
    }

    @Override
    public boolean sunFallsFromSky() { return false; }

}
