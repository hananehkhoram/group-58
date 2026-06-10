package model.season;

import model.GameContext;

import java.util.List;

public class FrozenCaveChapter extends Season {

    @Override
    public boolean iceEffectiveOnZombies() { return false; }

    @Override
    public void onWaveStart(GameContext ctx, int waveNumber) {
    }

    private List<Integer> pickRandomRows(GameContext ctx) { return null;}
}
