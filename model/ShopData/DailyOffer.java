package model.ShopData;

public class DailyOffer{
    private int id;
    private String plantType;
    private int discountedPrice;  // 1600 سکه
    private long expiryDate;
    private boolean purchased;
    private int amount = 10;
    private int price = 1600;
    private Currency currency = Currency.COIN;

    public DailyOffer(String plantType,int discountedPrice,long expiryDate){}

    public String getPlantType() {
        return plantType;
    }

    public int getDiscountedPrice() {
        return discountedPrice;
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
}