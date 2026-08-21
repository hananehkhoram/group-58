package com.workshop.model.GreenHouseData;


public class GreenHouse {
    public static final int ROWS = 3;
    public static final int COLS = 4;

    private Pot[][] pots;

    public GreenHouse() {
        this.pots = new Pot[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                pots[i][j] = new Pot();
                pots[i][j].setLocked(i != 1);
            }
        }
    }

    public boolean unlockPot(int x, int y) {
        if (isInvalidBounds(x, y)) return false;
        Pot pot = pots[x][y];
        if (pot.isLocked()) {
            pot.setLocked(false);
            return true;
        }
        return false;
    }

    public boolean unlockFirstLockedPot() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (pots[i][j].isLocked()) {
                    pots[i][j].setLocked(false);
                    return true;
                }
            }
        }
        return false;
    }

    public Pot getPot(int x, int y) {
        if (isInvalidBounds(x, y)) return null;
        return pots[x][y];
    }

    private boolean isInvalidBounds(int x, int y) {
        return x < 0 || x >= ROWS || y < 0 || y >= COLS;
    }
}
