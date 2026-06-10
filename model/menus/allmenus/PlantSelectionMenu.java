package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class PlantSelectionMenu extends BaseMenu {
    public PlantSelectionMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.SELECT_PLANTS);
    }

    public void showAllPlants() {}
    public void showAvailablePlants() {}
    public void addPlant(String plantType) {}
    public void removePlant(String plantType) {}
    public void boostPlant(String plantType) {}
    public void startGame() {}
}
