package model.ShopData;

public class ShopItem{
    private int id;
    private String name;
    private int priceCoins;
    private int priceGems;
    private int maxPurchase;// 0 means unlimited
    private ItemType type;
    private int value;
    private String plantType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriceCoins() {
        return priceCoins;
    }

    public void setPriceCoins(int priceCoins) {
        this.priceCoins = priceCoins;
    }

    public int getPriceGems() {
        return priceGems;
    }

    public void setPriceGems(int priceGems) {
        this.priceGems = priceGems;
    }

    public int getMaxPurchase() {
        return maxPurchase;
    }

    public void setMaxPurchase(int maxPurchase) {
        this.maxPurchase = maxPurchase;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getPlantType() {
        return plantType;
    }

    public void setPlantType(String plantType) {
        this.plantType = plantType;
    }
}
