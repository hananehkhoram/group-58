package model.season;

import model.GameContext;
import model.GridCell;

import java.util.Set;

public class BigWaveBeachSeason extends Season{
        private int currentWaterColumns;
        private int maxWaterColumns;

        @Override
        public Set<GridCell> getWaterCells(GameContext ctx) {return null;}

        @Override
        public void onWaveStart(GameContext ctx, int waveNumber) {

        }

        private void updateWaterLevel(GameContext ctx, int newLevel) {
        }

}
