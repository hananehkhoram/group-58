package controller;

import controller.commandHandler.CommandRegistry;
import exceptions.CommandNotFound;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.menus.Menu;
import model.menus.MenuType;
import model.menus.allmenus.*;
import model.season.Season;
import view.ConsoleView;

import java.util.*;

public class MenuManager {
    private GameContext ctx;
    private Menu currentMenu;
    private GameEngine gameEngine;

    private static final Map<MenuType, Set<MenuType>> ALLOWED_ENTRIES = new EnumMap<>(MenuType.class);
    private static final Map<MenuType, MenuType> EXIT_TARGET = new EnumMap<>(MenuType.class);

    public static MenuType getExitTarget(MenuType type) {
        return EXIT_TARGET.get(type);
    }


    static {
        ALLOWED_ENTRIES.put(MenuType.REGISTER, EnumSet.of(MenuType.LOGIN));
        ALLOWED_ENTRIES.put(MenuType.MAIN, EnumSet.of(MenuType.GAME, MenuType.SETTINGS, MenuType.NEWS, MenuType.PROFILE));
        ALLOWED_ENTRIES.put(MenuType.GAME, EnumSet.of(MenuType.COLLECTION,MenuType.SELECT_PLANTS,MenuType.GREENHOUSE,MenuType.TRAVEL,MenuType.LEADERBOARD));
        // LOGIN -> MAIN عمداً اینجا نیست؛ اون انتقال خودکار توسط دستور login موفق انجام می‌شه، نه menu enter دستی

        EXIT_TARGET.put(MenuType.LOGIN, MenuType.REGISTER);
        EXIT_TARGET.put(MenuType.GAME, MenuType.MAIN);
        EXIT_TARGET.put(MenuType.SETTINGS, MenuType.MAIN);
        EXIT_TARGET.put(MenuType.NEWS, MenuType.MAIN);
        EXIT_TARGET.put(MenuType.PROFILE, MenuType.MAIN);
        EXIT_TARGET.put(MenuType.COLLECTION, MenuType.GAME);
    }


    public MenuManager(GameContext ctx) {
        this.ctx = ctx;
        this.currentMenu = new RegisterMenu(this.ctx);
    }
    private Menu buildMenu(String targetMenu) {
        MenuType type = MenuType.fromMenuName(targetMenu);
        if (type == null) {
            throw new CommandNotFound("Invalid menu type!");
        }
        switch (type) {
            case LOGIN: return new LoginMenu(ctx);
            case MAIN: return new MainMenu(ctx);
            case SHOP: return new ShopMenu(ctx);
            case COLLECTION: return new CollectionMenu(ctx);
            case GAME: return new GameMenu(ctx);
            case GREENHOUSE: return new GreenHouseMenu(ctx);
            case LEADERBOARD: return new LeaderBoardMenu(ctx);
            case NEWS: return new NewsMenu(ctx);
            case SELECT_PLANTS: return new PlantSelectionMenu(ctx);
            case PROFILE: return new ProfileMenu(ctx);
            case REGISTER: return new RegisterMenu(ctx);
            case TRAVEL: return new TravelMenu(ctx);
            default: throw new CommandNotFound("Invalid menu type!");
        }
    }

    public void changeMenu(String targetMenu){
        Menu newMenu = buildMenu(targetMenu);

        MenuType requested = newMenu.getMenu();
        MenuType current = (currentMenu == null) ? null : currentMenu.getMenu();

        if (current != null) {
            Set<MenuType> allowed = ALLOWED_ENTRIES.getOrDefault(current, Collections.emptySet());
            if (!allowed.contains(requested)) {
                throw new CommandNotFound("You can't go to " + targetMenu + " from here.");
            }
        }

        currentMenu = newMenu;
    }

    public void forceChangeMenu(String targetMenu) {
        currentMenu = buildMenu(targetMenu);
    }
    public void update(){}
    public void handleInput(){}
    public Menu getCurrentMenu(){
        return currentMenu;
    }
    public void startBattle(Level level, Season season) {
        this.ctx = new GameContext(level, season);
        this.gameEngine = new GameEngine(this.ctx);
    }

    public GameEngine getGameEngine() { return gameEngine; }

    public GameContext getCtx() {
        return ctx;
    }

    public void setCtx(GameContext ctx) {
        this.ctx = ctx;
    }

    public  void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

}
