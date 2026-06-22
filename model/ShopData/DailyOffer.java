package model.ShopData;

import model.plants.Plant;

public class DailyOffer{
    private final int id = 6;
    private String name;
    private Plant plantType;
    private long expiryDate;
    private boolean purchased;
    private int amount = 10;
    private final int price = 1600;
    private final Currency currency = Currency.COIN;

    public DailyOffer(Plant plantType) {
        this.plantType = plantType;
        this.purchased = false;
        this.name = plantType.getName();
    }

    public Plant getPlantType() {
        return plantType;
    }


    public long getExpiryDate() {
        return expiryDate;
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
}