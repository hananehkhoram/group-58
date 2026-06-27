package controller;

import controller.commandHandler.CommandRegistry;
import model.GameContext;
import model.menus.Menu;

import java.util.Stack;

public class MenuManager {
    private Stack<Menu> menus =  new Stack<>();
    private GameContext ctx;
    private Menu nextMenu;

    public void pushMenu(Menu menu) {
        menus.push(menu);
    }
    public void popMenu(){
        if(!menus.isEmpty()){
            menus.pop();
        }
    }
    public void changeMenu(String targetMenu){}
    public void update(){}
    public void handleInput(){}
    public Menu getCurrentMenu(){
        if (!menus.isEmpty()){
            return menus.peek();
        }else {
            return null;
        }
    }
}
