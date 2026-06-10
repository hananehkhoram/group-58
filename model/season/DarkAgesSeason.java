package model.season;

import model.GameContext;

public class DarkAgesSeason extends Season{

        @Override
        public boolean sunFallsFromSky() { return false; }

        @Override
        public void onWaveStart(GameContext ctx, int waveNumber) {

        }

        private void spawnRandomGraves(GameContext ctx) {

        }
}
