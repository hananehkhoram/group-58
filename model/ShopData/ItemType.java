package model.ShopData;

public enum ItemType {

    POT_UNLOCK("Unlock New Pot", 2000,Currency.COIN,1,
            "Unlocks a new pot in your greenhouse."),
    PLANT_FOOD("Plant Food", 3,Currency.GEM,1,
            "Boosts your plant's growth speed."),
    RANDOM_SEED_PACK("Random Seed Pack", 1000,Currency.COIN,5,
            "5 Random Seed Packs for a random plant from unlocked plants."),
    SELECTED_SEED_PACK("Selected Seed Pack", 5,Currency.GEM,10,
            "10 Random Seed Packs for a selected plant from unlocked plants."),
    CURRENCY_CONVERSION("Currency Conversion", 5,Currency.GEM,500,
            "Convert your gems into coins.");


    private final String displayName;
    private final int price;
    private Currency currency;
    private final String description;
    private int amount;

    ItemType(String displayName, int price,Currency currency,int amount, String description) {
        this.displayName = displayName;
        this.price = price;
        this.currency = currency;
        this.description = description;
        this.amount = amount;
    }


    public String getDisplayName() {
        return displayName;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public Currency getCurrency() {
        return currency;
    }

    public int getAmount() {
        return amount;
    }
}