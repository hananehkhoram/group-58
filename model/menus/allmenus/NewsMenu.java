package model.menus.allmenus;

import controller.NewsManager;
import controller.repository.DataManager;
import model.GameContext;
import model.News.News;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;
import model.user.UserManager;

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
