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

        allUsers.sort((u1, u2) -> Integer.compare(u2.getMaxMewPoint(), u1.getMaxMewPoint()));

        int rank = 1;
        for (User user : allUsers) {
            view.ConsoleView.simplePrint(rank + "- " + user.getUsername() +
                                            " | Score: " + user.getMaxMewPoint() +
                                            " | Season: " + user.getLastSeason() +
                                            " | Level: " + user.getLastLevel() + "\n");
            rank++;
        }

        view.ConsoleView.simplePrint("\n==========================================\n");
    }
}
