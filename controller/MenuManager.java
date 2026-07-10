package controller;

import controller.commandHandler.CommandRegistry;
import exceptions.CommandNotFound;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.menus.Menu;
import model.menus.allmenus.*;
import model.season.Season;

import java.util.Stack;

public class MenuManager {
    private Stack<Menu> menus =  new Stack<>();
    private GameContext ctx;
    private Menu nextMenu;
    private Menu currentMenu;
    private GameEngine gameEngine;

    public MenuManager(GameContext ctx) {
        this.ctx = ctx;
        this.menus.push(new RegisterMenu(this.ctx));
    }

    public void pushMenu(Menu menu) {
        menus.push(menu);
    }
    public void popMenu(){
        if(!menus.isEmpty()){
            menus.pop();
        }
    }
    public void changeMenu(String targetMenu){
        Menu newMenu = null;

        switch (targetMenu.toLowerCase()) {
            case "loginmenu":
                newMenu = new LoginMenu(ctx);
                break;
            case "mainmenu":
                newMenu = new MainMenu(ctx);
                break;
            case "shopmenu":
                newMenu = new ShopMenu(ctx);
                break;
            case "collectionmenu":
                newMenu = new CollectionMenu(ctx);
                break;
            case "gamemenu":
                newMenu = new GameMenu(ctx);
                break;
            case "greenhousemenu":
                newMenu = new GreenHouseMenu(ctx);
                break;
            case "leaderboardmenu":
                newMenu = new LeaderBoardMenu(ctx);
                break;
            case "newsmenu":
                newMenu = new NewsMenu(ctx);
                break;
            case "plantselectionmenu":
                newMenu = new PlantSelectionMenu(ctx);
                break;
            case "profilemenu":
                newMenu = new ProfileMenu(ctx);
                break;
            case "registermenu":
                newMenu = new RegisterMenu(ctx);
                break;
            case "travelmenu":
                newMenu = new TravelMenu(ctx);
                break;
            default:
                throw new CommandNotFound("Invalid menu type!");
        }

        if (newMenu != null) {
            if (!menus.isEmpty()) {
                menus.pop(); // حذف منوی قبلی از راس پشته
            }
            menus.push(newMenu); // اضافه کردن منوی جدید
        } else {
            System.out.println("Error: Menu '" + targetMenu + "' not found!");
        }
    }
    public void update(){}
    public void handleInput(){}
    public Menu getCurrentMenu(){
        if (!menus.isEmpty()){
            return menus.peek();
        }else {
            return null;
        }
    }
    public void startBattle(Level level, Season season) {
        this.ctx = new GameContext(level, season);
        this.gameEngine = new GameEngine(this.ctx);
    }

    public GameEngine getGameEngine() { return gameEngine; }

    public GameContext getCtx() {
        return ctx;
    }
}
