package model.menus.allmenus;

import controller.repository.DataManager;
import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoardMenu extends BaseMenu {

    private GameContext gameContext;

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
        view.ConsoleView.simplePrint("=============== LEADERBOARD ================\n");

        List<User> allUsers = new ArrayList<>(DataManager.getInstance().users.getUserMap().values());

        allUsers.sort((u1, u2) -> {
            int scoreCompare = Integer.compare(u2.getMaxMewPoint(), u1.getMaxMewPoint());
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

            view.ConsoleView.simplePrint(rank + "- " + user.getUsername() +
                    " | Score: " + user.getMaxMewPoint() +
                    " | Season: " + user.getLastSeason() +
                    " | Level: " + user.getLastLevel() +
                    " | Quests: " + completedQuestsCount +
                    " | Minigames: " + minigamesCount + "\n");
            rank++;
        }

        view.ConsoleView.simplePrint("\n==========================================\n");
    }
}