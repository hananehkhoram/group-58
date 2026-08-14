package com.workshop.model.menus.allmenus;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.GameContext;
import com.workshop.model.GreenHouseData.GreenHouse;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.plants.Plant;
import com.workshop.model.shopData.Currency;
import com.workshop.model.shopData.DailyOffer;
import com.workshop.model.shopData.ItemType;
import com.workshop.model.shopData.Shop;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;

import java.util.Random;

public class ShopMenu extends BaseMenu {
    private Shop shop;
    private User currentUser;
    private UserManager um;
    private Random random = new Random();
    private GreenHouse greenHouse;


    public ShopMenu(GameContext ctx) {
        super(ctx, MenuType.SHOP);
        this.um = UserManager.getInstance();
        this.currentUser = um.getCurrentUser();
        this.shop = new Shop();
        shop.updateDailyOffer(currentUser);
        this.greenHouse = currentUser.getGreenHouse();
        this.name = "Shop menu";
    }

    public String showShopList(){
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Shop ===\n");

        for (ItemType item : shop.getPermanentItems()){
            sb.append("-----------\n");
            sb.append("Id: ").append(item.getId()).append("\n");
            sb.append(item.getDisplayName()).append(" Price: ").append(item.getPrice());
            sb.append(item.getCurrency().equals(Currency.COIN) ? " coins.\n" : " gems.\n");
            sb.append("Description: ").append(item.getDescription()).append("\n");
            sb.append("-----------\n");
        }
        return sb.toString();
    }
    public String showDailyOffer(){
        var offer = shop.getDailyOffer();
        StringBuilder sb = new StringBuilder();
        if (offer != null) {
            if (offer.isPurchased()) {
                return ("You have already bought today's offer!");
            } else {
                sb.append("=== Daily offer ===\n");
                sb.append("-----------\n");
                sb.append("Today's special plant seed: ").append(shop.getDailyOffer().getPlantType().getName());
                sb.append("\nId: ").append(shop.getDailyOffer().getId());
                sb.append("\nPrice: ").append(shop.getDailyOffer().getPrice()).append("\n");
                sb.append("-----------\n");

            }
        }
        return sb.toString();
    }
    public String buyItem(int id, int count, String plantType){
        if (count <= 0) {
            return "Invalid count.";
        }

        ItemType item = shop.getItemById(id);
        if (item == null) {
            return id == 0 ? buyDailyOffer(count) : "Invalid id.";
        }

        String stockError = checkStockLimit(item, count);
        if (stockError != null) return stockError;

        String chargeError = chargeCurrency(item, count);
        if (chargeError != null) return chargeError;

        return switch (item) {
            case POT_UNLOCK -> completePotUnlockPurchase(count);
            case PLANT_FOOD -> completePlantFoodPurchase(count);
            case CURRENCY_CONVERSION -> completeCurrencyConversion(item, count);
            case RANDOM_SEED_PACK -> completeRandomSeedPackPurchase(item, count);
            case SELECTED_SEED_PACK -> completeSelectedSeedPackPurchase(item, count, plantType);
        };
    }

    /** محدودیت‌های موجودی (مثل حداکثر تعداد گلدان یا پلانت‌فود) را چک می‌کند. */
    private String checkStockLimit(ItemType item, int count) {
        if (item == ItemType.POT_UNLOCK && currentUser.getOwnedPotsCount() + count > GreenHouse.ROWS * GreenHouse.COLS) {
            return "Purchase failed! You cannot own more than " + (GreenHouse.ROWS * GreenHouse.COLS) + " pots.";
        }
        return null;
    }

    /** بر اساس نوع ارز، مبلغ رو از کاربر کم می‌کند؛ اگر پول کافی نبود پیام خطا برمی‌گرداند. */
    private String chargeCurrency(ItemType item, int count) {
        int totalCost = item.getPrice() * count;
        if (item.getCurrency() == Currency.COIN) {
            if (currentUser.getCoins() < totalCost) return "Not enough coins!";
            currentUser.setCoins(currentUser.getCoins() - totalCost);
        } else if (item.getCurrency() == Currency.GEM) {
            if (currentUser.getGems() < totalCost) return "Not enough gems!";
            currentUser.setGems(currentUser.getGems() - totalCost);
        }
        return null;
    }

    private String completePotUnlockPurchase(int count) {
        currentUser.setOwnedPotsCount(currentUser.getOwnedPotsCount() + count);
        if (!greenHouse.unlockFirstLockedPot()) {
            return "Purchase failed! All pots in your greenhouse are already unlocked.";
        }
        DataManager.getInstance().saveUser();
        return "Successfully unlocked " + count + " new pot(s) in your greenhouse!";
    }

    private String completePlantFoodPurchase(int count) {
        currentUser.setPlantFoodCount(currentUser.getPlantFoodCount() + count);
        DataManager.getInstance().saveUser();
        return "Successfully purchased " + count + " Plant Food(s)! Total: " + currentUser.getPlantFoodCount();
    }

    private String completeCurrencyConversion(ItemType item, int count) {
        int coinsGained = item.getAmount() * count;
        currentUser.setCoins(currentUser.getCoins() + coinsGained);
        DataManager.getInstance().saveUser();
        return "Successfully converted gems to " + coinsGained + "cons.";
    }

    private String completeRandomSeedPackPurchase(ItemType item, int count) {
        Plant plant = currentUser.getRandomUnlockedPlant();
        if (plant == null) {
            return "Purchase failed! You don't have any unlocked plants to receive seeds for.";
        }
        int seedsGained = item.getAmount() * count;
        currentUser.addSeedsToInventory(plant.getName(), seedsGained);
        DataManager.getInstance().saveUser();
        return "Successfully purchased " + count + "x " + item.getDisplayName() +
                "! You received " + seedsGained + " seeds for: " + plant.getName();
    }

    private String completeSelectedSeedPackPurchase(ItemType item, int count, String plantType) {
        if (plantType == null || plantType.isEmpty()) {
            return "You must specify which plant seed you want to buy!";
        }

        String officialPlantName = findUnlockedPlantName(plantType);
        if (officialPlantName == null) {
            return "Purchase failed! You haven't unlocked " + plantType + " yet.";
        }

        int totalSeedsGained = item.getAmount() * count;
        currentUser.addSeedsToInventory(officialPlantName, totalSeedsGained);
        DataManager.getInstance().saveUser();
        return "Successfully purchased " + count + "x " + item.getDisplayName() +
                " for " + officialPlantName + "! Gained " + totalSeedsGained + " seeds.";
    }

    private String findUnlockedPlantName(String plantType) {
        for (Plant p : currentUser.getUnlockedPlantTypes()) {
            if (p.getName().equalsIgnoreCase(plantType)) {
                return p.getName();
            }
        }
        return null;
    }

    public String buyDailyOffer(int count){
        DailyOffer offer = currentUser.getLastDailyOffer();
        if (offer == null) {
            return "No daily offer available today.";
        }
        if (offer.isPurchased()) {
            return "You have already purchased today's daily offer!";
        }
        if (count > 1) {
            return "You can only buy 1 pack of the daily offer per day.";
        }


        int totalCost = offer.getPrice();
        if (currentUser.getCoins() < totalCost) return "Not enough coins!";
        currentUser.setCoins(currentUser.getCoins() - totalCost);

        currentUser.addSeedsToInventory(offer.getName(), offer.getAmount());

        offer.setPurchased(true);

//        um.saveToFile();
        DataManager.getInstance().saveUser();
        return "Successfully purchased today's special offer";
    }
}
