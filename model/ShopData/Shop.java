package model.ShopData;

import model.user.User;

import java.util.List;

public class Shop{
    private List<ShopItem> permanentItems;
    private DailyOffer dailyOffer;

    public Shop() {}

    public List<ShopItem> getPermanentItems() {
        return permanentItems;
    }

    public DailyOffer getDailyOffer() {
        return dailyOffer;
    }
    public boolean buyItem(User user, String id, int count, String plantType){return false;}
    public void refreshDailyOffer(User user){}
}

