package model.GreenHouseData;

import model.user.User;

public class GreenHouse{
    private Pot[][] pots;

    public GreenHouse() {
        this.pots = new Pot[4][5];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                pots[i][j] = new Pot();
                pots[i][j] = new Pot();
                if (i == 1) {
                    pots[i][j].setLocked(false);
                } else {
                    pots[i][j].setLocked(true);
                }
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
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
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
        return x < 0 || x >= 4 || y < 0 || y >= 5;
    }


}