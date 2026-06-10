package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.UserManager;

public class RegisterMenu extends BaseMenu {
    public RegisterMenu(String name, GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.REGISTER);
    }
    private UserManager um;
    public void register(String username, String password){} //and others
    public void pickQuestion(String answer,int questionNUmber,String trueAnswer){}
    public void goToLoginMenu(){}

}
