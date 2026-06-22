package model.GreenHouseData;

public class GreenHouse{
    private Pot[][] pots;

    public GreenHouse() {
        this.pots = new Pot[3][4];
        for (Pot pot : pots[1]){
            pot.setLocked(false);
        }
    }

    public boolean unlockPot(int x, int y){return false;}
    public boolean plantPot(int x,int y){return false;}
    public Pot getPot(int x,int y){return null;}
    public boolean accelerateGrowth(int x,int y){return false;}
    public int collect(int x,int y){return 0;}
}