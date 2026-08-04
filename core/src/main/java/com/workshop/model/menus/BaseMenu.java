package com.workshop.model.menus;

import com.workshop.model.GameContext;

public abstract class BaseMenu implements Menu{
    protected GameContext ctx;
    protected String name;
    protected MenuType menuType;

    public BaseMenu(GameContext ctx, MenuType menuType){
        this.ctx = ctx;
        this.menuType = menuType;
    }

    @Override
    public String showMenu(){
        return name;
    }
    public MenuType getMenu() { return menuType; }

}
