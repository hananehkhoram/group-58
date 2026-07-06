package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class LeaderBoardMenu extends BaseMenu {

    public LeaderBoardMenu(GameContext ctx) {
        super(ctx, MenuType.LEADERBOARD);
        this.name = "LeaderBoard menu";
    }

}
