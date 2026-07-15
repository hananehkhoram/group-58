package model.shopData;
 public enum ItemType {

    POT_UNLOCK("Unlock New Pot", 2000,Currency.COIN,1,1,
            "Unlocks a new pot in your greenhouse.",20),
    PLANT_FOOD("Plant Food", 3,Currency.GEM,1,2,
            "Boosts your plant's growth speed.",3),
    RANDOM_SEED_PACK("Random Seed Pack", 1000,Currency.COIN,5,3,
            "5 Random Seed Packs for a random plant from unlocked plants.",0),
    SELECTED_SEED_PACK("Selected Seed Pack", 5,Currency.GEM,10,4,
            "10 Random Seed Packs for a selected plant from unlocked plants.",0),
    CURRENCY_CONVERSION("Currency Conversion", 5,Currency.GEM,500,5,
            "Convert your gems into coins.",0);


    private final String displayName;
    private final int price;
    private final Currency currency;
    private final String description;
    private final int amount;
    private final int id;
    private final int maxPurchase;//zero means unlimited

    ItemType(String displayName, int price,Currency currency,
             int amount,int id, String description,int maxPurchase) {
        this.displayName = displayName;
        this.price = price;
        this.currency = currency;
        this.description = description;
        this.amount = amount;
        this.id = id;
        this.maxPurchase = maxPurchase;
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

    public int getId() {
        return id;
    }
}