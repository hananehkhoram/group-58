package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;
import model.user.UserManager;

public class LoginMenu extends BaseMenu {
    private UserManager um;


    private enum ResetState { NONE, AWAITING_SECURITY_ANSWER, AWAITING_NEW_PASSWORD }

    private ResetState currentState = ResetState.NONE;
    private User targetUser = null;

    public LoginMenu(GameContext ctx) {
        super(ctx, MenuType.LOGIN);
        this.um = UserManager.getInstance();
        this.name = "Login menu";
    }

    public String login (String username,  String password,String stayLoggedIn){
        if (!um.doesUserExist(username)) return "User does not exist!";
        if (!um.isPasswordCorrect(password,username)) return "Incorrect password.";

        User user = um.findUserByName(username);

        um.login(user);
        if (stayLoggedIn != null) {
            user.setStayedLogin(true);
        } else {
            user.setStayedLogin(false);
        }
        return "Logged in successfully.";
    }

    public String answerSecurityQuestion(String answer){
        if (currentState != ResetState.AWAITING_SECURITY_ANSWER) {
            return "You are not in the password recovery process.";
        }

        if (targetUser.getSecurityAnswer().equalsIgnoreCase(answer)) {
            this.currentState = ResetState.AWAITING_NEW_PASSWORD;
            return "Answer is correct! Please enter your new password using: set-password <new_password>";
        } else {
            resetRecovery();
            return "Error: Incorrect answer! Password recovery canceled.";
        }
    }
    public String updatePassword(String newPassword){
        if (currentState != ResetState.AWAITING_NEW_PASSWORD) {
            return "Error: You cannot change password right now.";
        }
        if (!um.isPasswordValid(newPassword)) return "Invalid password format.";
        String passwordValidation = um.isPasswordStrong(newPassword);
        if (!passwordValidation.equals("ok")) return passwordValidation;

        um.changePassword(newPassword);
        resetRecovery();
        return "Password changed successfully.";
    }

    public String startForgetPasswordProcess(String username, String email) {
        User user = um.findUserByName(username);
        if (!um.doesUserExist(username)) return "User does not exist!";
        if (!um.isEmailCorrect(email,name)) return "Email is incorrect.";

        this.targetUser = user;
        this.currentState = ResetState.AWAITING_SECURITY_ANSWER;

        return "Security Question: " + user.getSecurityQuestion() + "\nPlease enter your answer using: answer <your_answer>";
    }

    private void resetRecovery() {
        this.currentState = ResetState.NONE;
        this.targetUser = null;
    }

}
