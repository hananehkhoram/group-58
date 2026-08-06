package com.workshop.model.season.miniGameSeason;

import com.workshop.model.level.Level;
import com.workshop.model.season.Season;

import java.util.List;

public class IzombieSeason extends Season {
    public IzombieSeason(List<Level> levels) {
        super("I, Zombie", levels, 0);
    }


    @Override
    public boolean sunFallsFromSky() {
        return false;
    }
}
