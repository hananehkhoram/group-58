package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.Security;
import model.user.SecurityQuestions;
import model.user.UserManager;

public class RegisterMenu extends BaseMenu {
    private UserManager um;
    public RegisterMenu(GameContext ctx) {
        super(ctx, MenuType.REGISTER);
        this.um = UserManager.getInstance();
        this.name = "Register menu";
    }


    public String register(String username, String password, String passwordConfirm, String nickName,
                         String email,String gender){
        if (!um.isUsernameValid(username)) return "Invalid username format.";
        if (um.doesUserExist(username)) return "Username already exists.";
        if (!um.isPasswordValid(password)) return "Invalid password format.";
        String passwordValidation = um.isPasswordStrong(password);
        if (!passwordValidation.equals("ok")) return passwordValidation;
        if (!um.doesPasswordsMatch(password,passwordConfirm)) {
            return "Passwords do not match! please try again.";
        }
        if (!um.isNickNameValid(nickName)) return "Invalid nickname.";
        if (!um.isEmailValid(email)) return "Invalid email format.";
        if (!gender.equalsIgnoreCase("female") && !gender.equalsIgnoreCase("male"))
            return "Invalid gender type.";

        um.register(username,password,nickName,email,gender);

        return "Please pick a security question.";

    }
    public String pickQuestion(int questionNumber,String answer,String answerConfirm){
        SecurityQuestions selectedQuestion = SecurityQuestions.getQuestionById(questionNumber);

        if (selectedQuestion == null) {
            return "Invalid question number! Please choose a valid number.";
        }
        if (!answer.equalsIgnoreCase(answerConfirm)){
            return "Answers don't mach!please try again.\n";
        }
        um.addQuestion(selectedQuestion,answer);
        return "Registered successfully.";
    }

}
