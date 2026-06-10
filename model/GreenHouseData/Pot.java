package model.GreenHouseData;

public class Pot{
    private boolean  locked;
    private String plantType;
    private long remainingPlantedTime;

    public boolean isEmpty(){return false;}
    public boolean isReady(){return false;}
    public long getRemainingPlantedTime() {
        return remainingPlantedTime;
    }
    public void plant(String type,long time){}
    public void collectPlant(){}
}
