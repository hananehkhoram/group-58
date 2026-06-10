package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class LoginMenu extends BaseMenu {
    public LoginMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.LOGIN);
    }

    public void login (String username,  String password){}
    public boolean checkUsernameExists(String username){return false;}
    public boolean verifySecurityAnswer(String username,String answer){ return false;}
    public void updatePassword(String username,String newPassword){}


    public void forgetPassword(String username, String email){}
    public void answerSecurityQuestion(String answer){}

}
