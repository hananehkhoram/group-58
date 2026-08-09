package com.workshop.model.season.miniGameSeason;

import com.workshop.model.level.Level;
import com.workshop.model.season.Season;

import java.util.List;

public class VaseSeason extends Season {

    public VaseSeason(List<Level> levels) {
        super("Vasebreaker", levels, 0);
    }

    @Override
    public boolean sunFallsFromSky() {
        return false;
    }
}
