package model.shopData;

import model.plants.Plant;

public class DailyOffer{
    private static int id_counter = 0; //Hananeh's Comment
    private int id;
    private String name;
    private Plant plantType;
    private long date;
    private boolean purchased;
    private int amount = 10;
    private final int price = 1600;
    private final Currency currency = Currency.COIN;

    public DailyOffer(Plant plantType) {
        this.plantType = plantType;
        this.purchased = false;
        this.name = plantType.getName();
        this.id = id_counter++; //Hananeh's Comment
    }

    // Hananeh's Comment
    public DailyOffer(int id , long date , boolean isPurchased) {
        this.id = id;
        this.date = date;
        this.purchased = isPurchased;
    }

    public Plant getPlantType() {
        return plantType;
    }

    public long getDate() {
        return date;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public int getAmount() {
        return amount;
    }

    public int getPrice() {
        return price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public int getId() {
        return id;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }
}