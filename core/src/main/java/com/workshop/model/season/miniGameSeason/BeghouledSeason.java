package com.workshop.model.season.miniGameSeason;

import com.workshop.model.level.Level;
import com.workshop.model.season.Season;

import java.util.List;

public class BeghouledSeason extends Season {

    public BeghouledSeason(List<Level> levels) {
        super("Beghouled", levels, 0);
    }

    @Override
    public boolean sunFallsFromSky() {
        return false;
    }
}
