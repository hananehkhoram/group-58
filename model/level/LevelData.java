package model.level;


import model.mechanisms.Wave;

public class LevelData {
    protected enum SpecialLevelType{
        NORMAL,//...
    }
    public class Level{
        protected String name;
        protected int rows;
        protected int columns;
        protected Wave[] waves;
        protected SpecialLevelType specialLevelType;
        protected Season season;
        protected boolean isLocked;

        public String getName() {
            return name;
        }

        public int getRows() {
            return rows;
        }

        public int getColumns() {
            return columns;
        }

        public Wave[] getWaves() {
            return waves;
        }

        public SpecialLevelType getSpecialLevelType() {
            return specialLevelType;
        }

        public Season getSeason() {
            return season;
        }
        public void unlock(){}

        public boolean isLocked() {
            return isLocked;
        }
    }
}

