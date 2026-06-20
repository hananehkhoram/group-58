package controller;

import model.GameContext;
import model.menus.Menu;

import java.util.Stack;

public class MenuManager {
    private Stack<Menu> menus;
    private GameContext ctx;
    private Menu nextMenu;

    public void pushMenu(){}
    public void popMenu(){}
    public void changeMenu(String targetMenu){}
    public void update(){}
    public void handleInput(){}
}
