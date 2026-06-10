package model.menus;

import model.GameContext;

public abstract class BaseMenu implements Menu{
    protected GameContext ctx;
    protected String name;
    protected MenuType menuType;

    public BaseMenu(GameContext ctx, MenuType menuType){
        this.ctx = ctx;
        this.menuType = menuType;
    }
    @Override
    public void update() {}

    @Override
    public void showMenu(){}
    public MenuType getMenu() {
        return menuType;
    }


}
