package com.workshop.model.GreenHouseData;

import com.workshop.model.plants.Plant;

public class Pot {
    private boolean locked = true;
    private boolean empty = true;
    private Plant plantType;
    private double remainingPlantedTime;
    private boolean plantReady;
    private boolean isMarigold;
    private long plantReadyTimestamp;

    public boolean isEmpty() {
        return empty;
    }

    public double getRemainingPlantedTime() {
        if (empty || isPlantReady()) {
            return 0;
        }
        long millisLeft = plantReadyTimestamp - System.currentTimeMillis();
        if (millisLeft <= 0) {
            setPlantReady(true);
            return 0;
        }
        return millisLeft / (1000.0 * 3600.0);
    }

    public void setPlantedHours(double hours) {
        this.plantReadyTimestamp = System.currentTimeMillis() + (long) (hours * 3600 * 1000);
        this.plantReady = false;
    }

    public void plant(Plant plant) {
        empty = false;
        plantType = plant;
        plantReady = false;
    }

    public void collectPlant() {
        empty = true;
        plantType = null;
        remainingPlantedTime = 0;
        plantReadyTimestamp = 0;
        plantReady = false;
        isMarigold = false;
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
        this.plantReadyTimestamp = System.currentTimeMillis() + (long) (remainingPlantedTime * 3600 * 1000);
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean isPlantReady() {
        if (!empty && !plantReady && plantReadyTimestamp > 0) {
            if (System.currentTimeMillis() >= plantReadyTimestamp) {
                plantReady = true;
            }
        }
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
