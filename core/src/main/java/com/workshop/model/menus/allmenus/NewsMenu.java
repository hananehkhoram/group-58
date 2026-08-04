package com.workshop.model.menus.allmenus;

import com.workshop.controller.NewsManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.GameContext;
import com.workshop.model.News.News;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;

public class NewsMenu extends BaseMenu {
    private UserManager um;
    public NewsMenu(GameContext ctx) {
        super(ctx, MenuType.NEWS);
        this.um = UserManager.getInstance();
        this.name = "News menu";
    }

    public String showUnreadNews() {
        User user = um.getCurrentUser();
        StringBuilder sb = new StringBuilder();
        for (News n : NewsManager.getAllNews()) {
            if (user.getLastReadNewsId() < n.getId()){
                sb.append(n.getTitle()).append(": ").append(n.getContent()).append("\n");
            }

        }

        int latestNewsId = NewsManager.getAllNews().size();
        user.setLastReadNewsId(latestNewsId);
//        um.saveToFile();
        DataManager.getInstance().saveUser();
        if (sb.isEmpty()) {
            return "No unread news available.";
        }
        return sb.toString();
    }

    public String showAllNews() {
        User user = um.getCurrentUser();
        StringBuilder sb = new StringBuilder();
        for (News n : NewsManager.getAllNews()) {
            sb.append(n.getTitle()).append(": ").append(n.getContent()).append("\n");
        }

        int latestNewsId = NewsManager.getAllNews().size();
        user.setLastReadNewsId(latestNewsId);
//        um.saveToFile();
        DataManager.getInstance().saveUser();
        if (sb.isEmpty()) {
            return "No news available.";
        }
        return sb.toString();
    }
}
