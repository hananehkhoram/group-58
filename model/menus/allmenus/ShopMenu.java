package model.menus.allmenus;

import model.GameContext;
import model.ShopData.Currency;
import model.ShopData.DailyOffer;
import model.ShopData.ItemType;
import model.ShopData.Shop;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;
import model.user.UserManager;

import java.util.Random;

public class ShopMenu extends BaseMenu {
    private Shop shop;
    private User currentUser;
    private UserManager um;
    private Random random = new Random();

    public ShopMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.SHOP);
        this.um = UserManager.getInstance();
        this.currentUser = um.getCurrentUser();
        this.shop = new Shop();
        shop.updateDailyOffer(currentUser);
    }

    public String showShopList(){
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Shop ===\n");

        for (ItemType item : shop.getPermanentItems()){
            sb.append("-----------\n");
            sb.append("Id: ").append(item.getId());
            sb.append(item.getDisplayName()).append("Price: ").append(item.getPrice());
            sb.append(item.getCurrency().equals(Currency.COIN) ? "coins.\n" : "gems.\n");
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
        String result = new String();
        if (count <= 0) {
            result = "Invalid count.";
        }
        ItemType item = shop.getItemById(id);
        if (item == null){
            if (id == 6) result = buyDailyOffer(count);
            else result = "Invalid id.";
        }
        else {
            if (item == ItemType.POT_UNLOCK) {
                if (currentUser.getOwnedPotsCount() + count > 20) {
                    return "Purchase failed! You cannot own more than 20 pots.";
                }
            }
            else if (item == ItemType.PLANT_FOOD) {
                if (currentUser.getPlantFoodCount() + count > 3) {
                    return "Purchase failed! You cannot hold more than 3 plant foods.";
                }
            }

            int totalCost = item.getPrice() * count;
            if (item.getCurrency() == Currency.COIN) {
                if (currentUser.getCoins() < totalCost) return "Not enough coins!";
                currentUser.setCoins(currentUser.getCoins() - totalCost);
            } else if (item.getCurrency() == Currency.GEM) {
                if (currentUser.getGems() < totalCost) return "Not enough gems!";
                currentUser.setGems(currentUser.getGems() - totalCost);
            }

            if (item == ItemType.POT_UNLOCK) {
                currentUser.setOwnedPotsCount(currentUser.getOwnedPotsCount() + count);
                //unlock pot
            }
            else if (item == ItemType.PLANT_FOOD) {
                currentUser.setPlantFoodCount(currentUser.getPlantFoodCount() + count);
                //add food
            }
            else if (item == ItemType.CURRENCY_CONVERSION) {
                int coinsGained = item.getAmount() * count;
                currentUser.setCoins(currentUser.getCoins() + coinsGained);
            }
            else if (item == ItemType.RANDOM_SEED_PACK) {
                // user.addRandomSeeds(selectedItem.getAmount() * count);
            }
            else if (item == ItemType.SELECTED_SEED_PACK) {
                //add seed
            }

            um.saveToFile();
        }
        return result;
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

        um.saveToFile();
        return "Successfully purchased today's special offer";
    }
}
