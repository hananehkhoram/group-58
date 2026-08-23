package com.workshop.model.menus.allmenus;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.GameContext;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.user.User;
import com.workshop.net.GameClient;
import com.workshop.net.UserSnapshot;


import java.util.ArrayList;
import java.util.List;

public class LeaderBoardMenu extends BaseMenu {

    public LeaderBoardMenu(GameContext ctx) {
        super(ctx, MenuType.LEADERBOARD);
        this.name = "LeaderBoard menu";
        printLeaderBoard();
    }

    @Override
    public MenuType getMenu(){
        return MenuType.LEADERBOARD;
    }

    private void printLeaderBoard() {
        com.workshop.view.Console.simplePrint("=============== LEADERBOARD ================\n");

        List<User> allUsers = loadUsers();

        allUsers.sort((u1, u2) -> {
            int scoreCompare = Integer.compare(myPointOrHidden(u2), myPointOrHidden(u1));
            if (scoreCompare != 0) return scoreCompare;

            int seasonCompare = Integer.compare(u2.getLastSeason(), u1.getLastSeason());
            if (seasonCompare != 0) return seasonCompare;

            int levelCompare = Integer.compare(u2.getLastLevel(), u1.getLastLevel());
            if (levelCompare != 0) return levelCompare;

            int quests1 = (u1.getCompletedQuestIds() != null) ? u1.getCompletedQuestIds().size() : 0;
            int quests2 = (u2.getCompletedQuestIds() != null) ? u2.getCompletedQuestIds().size() : 0;
            int questCompare = Integer.compare(quests2, quests1);
            if (questCompare != 0) return questCompare;

            return Integer.compare(u2.getMinigamesCompleted(), u1.getMinigamesCompleted());
        });

        int rank = 1;
        for (User user : allUsers) {

            int completedQuestsCount = (user.getCompletedQuestIds() != null) ? user.getCompletedQuestIds().size() : 0;
            int minigamesCount = user.getMinigamesCompleted();

            com.workshop.view.Console.simplePrint(rank + "- " + user.getUsername() +
                    " | My Point: " + formatMyPoint(user) +
                    " | Season: " + user.getLastSeason() +
                    " | Level: " + user.getLastLevel() +
                    " | Quests: " + completedQuestsCount +
                    " | Minigames: " + minigamesCount + "\n");
            rank++;
        }

        com.workshop.view.Console.simplePrint("\n==========================================\n");
    }

    private List<User> loadUsers() {
        GameClient client = GameClient.get();
        if (client.isConnected()) {
            List<User> fromServer = new ArrayList<>();
            for (UserSnapshot snap : client.getLeaderboard()) {
                User user = new User();
                user.setUsername(snap.username);
                snap.applyTo(user);
                fromServer.add(user);
            }
            if (!fromServer.isEmpty()) {
                return fromServer;
            }
        }
        return new ArrayList<>(DataManager.getInstance().users.getUserMap().values());
    }

    private static int myPointOrHidden(User user) {
        return user.hasNetworkBonusScore() ? user.getMaxMewPoint() : -1;
    }

    private static String formatMyPoint(User user) {
        return user.hasNetworkBonusScore() ? String.valueOf(user.getMaxMewPoint()) : "-";
    }
}
