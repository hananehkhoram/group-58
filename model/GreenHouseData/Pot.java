package model.GreenHouseData;

import model.plants.Plant;

public class Pot{
    private boolean  locked = true;
    private boolean empty = true;
    private Plant plantType;
    private double remainingPlantedTime;
    private boolean plantReady;
    private boolean isMarigold;

    public boolean isEmpty() {
        return empty;
    }

    public double getRemainingPlantedTime() {
        return remainingPlantedTime;
    }
    public void plant(Plant plant){
        empty = false;
        plantType = plant;
        plantReady = false;
    }
    public void collectPlant(){
        empty = true;
        plantType = null;
        remainingPlantedTime = 0;

    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Plant getPlantType() {
        return plantType;
    }

    public void setPlantType(Plant plantType) {
        this.plantType = plantType;
    }

    public void setRemainingPlantedTime(double remainingPlantedTime) {
        this.remainingPlantedTime = remainingPlantedTime;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean isPlantReady() {
        return plantReady;
    }

    public void setPlantReady(boolean plantReady) {
        this.plantReady = plantReady;
    }

    public boolean isMarigold() {
        return isMarigold;
    }

    public void setMarigold(boolean marigold) {
        isMarigold = marigold;
    }
}
