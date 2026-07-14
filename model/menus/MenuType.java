package model.menus;

public enum MenuType {
    REGISTER("registermenu"),
    LOGIN("loginmenu"),
    MAIN("mainmenu"),
    GAME("gamemenu"),
    SETTINGS("settingsmenu"),
    NEWS("newsmenu"),
    PROFILE("profilemenu"),
    COLLECTION("collectionmenu"),
    SELECT_PLANTS("plantselectionmenu"),
    LEADERBOARD("leaderboardmenu"),
    GREENHOUSE("greenhousemenu"),
    TRAVEL("travelmenu"),
    COIN_WALLET("coinwalletmenu"),
    GEM_WALLET("gemwalletmenu"),
    SHOP("shopmenu");

    private final String menuName;

    MenuType(String commandName) {
        this.menuName = commandName;
    }

    public String getMenuName() {
        return menuName;
    }

    public static MenuType fromMenuName(String name) {
        for (MenuType type : values()) {
            if (type.menuName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}