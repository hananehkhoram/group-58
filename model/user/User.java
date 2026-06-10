package model.user;


import model.GreenHouseData.GreenHouse;
import model.settings.Settings;

import java.util.List;

public class User {
    protected String username;
    protected String password;//hash it
    protected String nickName;
    protected String email;
    protected Gender gender;
    protected String securityQuestion;
    protected String securityAnswer;
    protected int gamesPlayed;
    protected int numberOfPassedLevels;
    protected int maxMewPoint;
    private Settings settings;
    private String lastDailyOfferPlant;   // ایدی گیاه پیشنهاد روز گذشته
    private long lastDailyOfferDate;      // تاریخ آخرین باری که پیشنهاد نمایش داده شد
    private boolean boughtDailyOfferToday;
    private GreenHouse greenHouse;
    private List<String> unlockedPlantTypes;     // گیاهانی که کاربر آنلاک کرده
    private int plantFoodCount;

    public boolean isBoughtDailyOfferToday() {
        return boughtDailyOfferToday;
    }

    public void setBoughtDailyOfferToday(boolean boughtDailyOfferToday) {
        this.boughtDailyOfferToday = boughtDailyOfferToday;
    }

    public long getLastDailyOfferDate() {
        return lastDailyOfferDate;
    }

    public void setLastDailyOfferDate(long lastDailyOfferDate) {
        this.lastDailyOfferDate = lastDailyOfferDate;
    }
    public void addSeedPackets(String plantType,int count){}
    public void getUnlockedPotCount(){}

    public int getPlantFoodCount() {
        return plantFoodCount;
    }

    public void setPlantFoodCount(int plantFoodCount) {
        this.plantFoodCount = plantFoodCount;
    }
    public Settings getSettings() { return settings; }
}

