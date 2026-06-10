package model.GreenHouseData;

public class GreenHouse{
    private Pot[] pots;

    public boolean unlockPot(int x,int y){return false;}//boolean to see if it succeeded
    public boolean plantPot(int x,int y){return false;}
    public Pot getPot(int x,int y){return null;}
    public boolean accelerateGrowth(int x,int y){return false;}
    public int collect(int x,int y){return 0;}
}