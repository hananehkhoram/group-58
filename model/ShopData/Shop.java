package model.ShopData;

import model.user.User;

import java.util.List;

public class Shop{
    private List<ItemType> permanentItems;
    private DailyOffer dailyOffer;

    public Shop() {}

    public List<ItemType> getPermanentItems() {
        return permanentItems;
    }

    public DailyOffer getDailyOffer() {
        return dailyOffer;
    }
    public boolean buyItem(User user, String id, int count, String plantType){return false;}
    public void refreshDailyOffer(User user){}
}

