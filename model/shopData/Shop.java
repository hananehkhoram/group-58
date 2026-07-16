package model.shopData;

import controller.repository.DataManager;
import model.plants.Plant;
import model.user.User;
import model.user.UserManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class Shop{
    private ItemType[] permanentItems;
    private UserManager um;
    protected User currentUser;

    public Shop() {
        this.um = UserManager.getInstance();
        this.currentUser = um.getCurrentUser();
        this.permanentItems = ItemType.values();

    }

    public ItemType[] getPermanentItems() {
        return permanentItems;
    }

    public DailyOffer getDailyOffer() {
        if (currentUser == null) return null;

        LocalDate today = LocalDate.now();

        if (currentUser.getLastDailyOfferDate() == null
                || !today.equals(currentUser.getLastDailyOfferDate())) {

            updateDailyOffer(currentUser);
        }

        return currentUser.getLastDailyOffer();
    }

    public void updateDailyOffer(User currentUser){
        Plant randomPlant = getRandomUnlockedPlant(currentUser);
        currentUser.setLastDailyOfferDate(LocalDate.now());
        DailyOffer newOffer = new DailyOffer(randomPlant);
        currentUser.setLastDailyOffer(newOffer);

//        um.saveToFile();
        DataManager.getInstance().saveUser();
    }

    private Plant getRandomUnlockedPlant(User currentUser) {
        List<Plant> unlockedPlants = currentUser.getUnlockedPlantTypes();

        if (unlockedPlants == null || unlockedPlants.isEmpty()) {
            return null;
        }

        Random random = new Random();

        int randomIndex = random.nextInt(unlockedPlants.size());

        return unlockedPlants.get(randomIndex);
    }

    public ItemType getItemById(int id) {
        for (ItemType item : ItemType.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}

