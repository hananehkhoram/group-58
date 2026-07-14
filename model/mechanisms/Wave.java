package model.mechanisms;

import model.GameContext;
import model.zombie.Zombie;

public class Wave{
    private GameContext ctx;
    private int waveNumber;
    private boolean isLastWave;
    private int waveCost;
    private int waveDelay;

    public int getWaveNumber() {
        return waveNumber;
    }

    public boolean isLastWave() {
        return isLastWave;
    }

    public int getWaveCost() {
        return waveCost;
    }

    public void setWaveCost(int waveCost) {
        this.waveCost = waveCost;
    }

    public int getWaveDelay() {return waveDelay;}

    public void setWaveNumber(int waveNumber) {
        this.waveNumber = waveNumber;
    }

    public void setLastWave(boolean lastWave) {
        isLastWave = lastWave;
    }

    public void setWaveDelay(int waveDelay) {
        this.waveDelay = waveDelay;
    }

    public Wave(int waveCost, int waveDelay, boolean isLastWave, int waveNumber) {
        this.waveCost = waveCost;
        this.waveDelay = waveDelay;
        this.isLastWave = isLastWave;
        this.waveNumber = waveNumber;
    }
}
