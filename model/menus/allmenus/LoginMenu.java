package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.SecurityQuestions;
import model.user.User;
import model.user.UserManager;

public class LoginMenu extends BaseMenu {
    private UserManager um;
    public LoginMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.LOGIN);
        this.um = UserManager.getInstance();
    }

    public String login (String username,  String password){
        if (um.doesUserExist(username)) return "User does not exist!";
        if (um.isPasswordCorrect(password,username)) return "Incorrect password.";

        User user = um.findUserByName(username);

        um.login(user);
        return "Logged in successfully.";
    }

    public String AnswerSecurityQuestion(String username,String answer){
        User user = um.findUserByName(username);
        if (!um.isAnswerCorrect(answer,user)) return "Wrong answer!";
        return "Please enter new password.";
    }
    public String updatePassword(String newPassword){
        if (!um.isPasswordValid(newPassword)) return "Invalid password format.";
        String passwordValidation = um.isPasswordStrong(newPassword);
        if (!passwordValidation.equals("ok")) return passwordValidation;

        um.changePassword(newPassword);
        return "Password changed successfully.";
    }


    public String forgetPassword(String username, String email){
        User user = um.findUserByName(username);
        if (!um.doesUserExist(username)) return "User does not exist!";
        if (!um.isEmailCorrect(email,name)) return "Email is incorrect.";
        return user.getSecurityQuestion().getQuestionText();
    }

}
