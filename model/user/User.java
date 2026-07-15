package model.user;


import model.GreenHouseData.GreenHouse;
import model.shopData.DailyOffer;
import model.plants.Plant;
import model.settings.Settings;
import model.zombie.Zombie;

import java.util.*;

public class User {
    protected String username;
    protected String password;//hash it
    protected String nickName;
    protected String email;
    protected Gender gender;
    protected SecurityQuestions securityQuestion;
    protected String securityAnswer;
    protected int gamesPlayed;
    protected int numberOfPassedLevels;
    protected int maxMewPoint;
    private Settings settings;
    private boolean stayedLogin;


    private Plant lastDailyOfferPlant;   // گیاه پیشنهاد روز گذشته
    private long lastDailyOfferDate;      // تاریخ آخرین باری که پیشنهاد نمایش داده شد
    private boolean boughtDailyOfferToday;
    private DailyOffer lastDailyOffer;

    private int ownedPotsCount = 1;
    private int plantFoodCount = 0;
    private Map<String, Boolean> storedBoosts = new HashMap<>();

    private Map<String, Integer> plantSeedsInventory = new HashMap<>();

    private GreenHouse greenHouse;
    private List<Plant> unlockedPlantTypes;// گیاهانی که کاربر آنلاک کرده
    private List<Zombie> seenZombies;

    protected int lastLevel;
    protected ArrayList<String> unlockedLevels;
    protected int lastSeason;

    private int difficultyLevel = 3;
    private int lastReadNewsId = 0;
    private int coins;
    private int gems;

    public User(String username, String password, String nickName, String email, Gender gender) {
        this.username = username;
        this.password = password;
        this.nickName = nickName;
        this.email = email;
        this.gender = gender;
        this.coins = 0;
        this.gems = 0;
        this.greenHouse = new GreenHouse();
        this.unlockedLevels = new ArrayList<>();
        this.unlockLevel("Ancient Egypt - Day 1");
        this.unlockedLevels = new ArrayList<>();
        this.unlockedPlantTypes = new ArrayList<>();
        this.seenZombies = new ArrayList<>();
    }

    public User() {
        this.unlockedLevels = new ArrayList<>();
        this.unlockLevel("Ancient Egypt - Day 1");
        this.unlockedLevels = new ArrayList<>();
        this.unlockedPlantTypes = new ArrayList<>();
        this.seenZombies = new ArrayList<>();
    }

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

    public int getLastLevel(){return lastLevel;}
    public int getLastSeason(){return lastSeason;}

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickName() {
        return nickName;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public SecurityQuestions getSecurityQuestion() {
        return securityQuestion;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getLastReadNewsId() {
        return lastReadNewsId;
    }

    public void setLastReadNewsId(int lastReadNewsId) {
        this.lastReadNewsId = lastReadNewsId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getCoins() {
        return coins;
    }

    public int getGems() {
        return gems;
    }

    public int getNumberOfPassedLevels (){return numberOfPassedLevels;}

    public int getMaxMewPoint() {
        return maxMewPoint;
    }

    public List<Plant> getUnlockedPlantTypes() {
        return unlockedPlantTypes;
    }

    public List<Zombie> getSeenZombies() {
        return seenZombies;
    }

    public void setNumberOfPassedLevels(int n) {this.numberOfPassedLevels = n;}

    public Plant getLastDailyOfferPlant() {
        return lastDailyOfferPlant;
    }

    public void setLastDailyOfferPlant(Plant lastDailyOfferPlant) {
        this.lastDailyOfferPlant = lastDailyOfferPlant;
    }

    public DailyOffer getLastDailyOffer() {
        return lastDailyOffer;
    }

    public void setLastDailyOffer(DailyOffer lastDailyOffer) {
        this.lastDailyOffer = lastDailyOffer;
    }

    public int getOwnedPotsCount() {
        return ownedPotsCount;
    }

    public void setOwnedPotsCount(int ownedPotsCount) {
        this.ownedPotsCount = ownedPotsCount;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setGems(int gems) {
        this.gems = gems;
    }
    public void addSeedsToInventory(String plantName, int amount) {
        int currentAmount = plantSeedsInventory.getOrDefault(plantName, 0);

        plantSeedsInventory.put(plantName, currentAmount + amount);

    }

    public GreenHouse getGreenHouse() {
        return greenHouse;
    }

    public int getSeedCount(String plantName) {
        return plantSeedsInventory.getOrDefault(plantName, 0);
    }
    public Plant getRandomUnlockedPlant(){
        if (unlockedPlantTypes == null || unlockedPlantTypes.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(unlockedPlantTypes.size());

        return unlockedPlantTypes.get(randomIndex);
    }
    public boolean hasStoredBoost(String plantName) {
        return storedBoosts.getOrDefault(plantName, false);
    }
    public void addStoredBoost(String plantName){
        String key = plantName.toLowerCase();

        storedBoosts.put(key, true);
    }

    public boolean isLevelUnlocked(String levelName) {
        return unlockedLevels.contains(levelName);
    }

    public void unlockLevel(String levelName) {
        if (!unlockedLevels.contains(levelName)) {
            unlockedLevels.add(levelName);
        }
    }

    public void setMaxMewPoint(int maxMewPoint) {this.maxMewPoint = maxMewPoint;}

    public void setUnlockedPlantTypes(List<Plant> unlockedPlantTypes) {
        this.unlockedPlantTypes = unlockedPlantTypes;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setSeenZombies(List<Zombie> seenZombies) {
        this.seenZombies = seenZombies;
    }

    public ArrayList<String> getUnlockedLevels() {
        return unlockedLevels;
    }

    public void setUnlockedLevels(ArrayList<String> unlockedLevels) {
        this.unlockedLevels = unlockedLevels;
    }

    public void setSecurityQuestion(SecurityQuestions securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public boolean isStayedLogin() {
        return stayedLogin;
    }

    public void setStayedLogin(boolean stayedLogin) {
        this.stayedLogin = stayedLogin;
    }

    public Map<String, Integer> getPlantSeedsInventory() {
        return plantSeedsInventory;
    }

    public Map<String, Boolean> getStoredBoosts() {
        return storedBoosts;
    }
}

