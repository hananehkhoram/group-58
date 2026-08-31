package com.workshop.model.menus.allmenus;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.GameContext;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.net.GameClient;
import com.workshop.net.NetResponse;
import com.workshop.net.UserSnapshot;
import com.workshop.model.user.SecurityQuestions;

public class LoginMenu extends BaseMenu {
    private UserManager um;


    private enum ResetState { NONE, AWAITING_SECURITY_ANSWER, AWAITING_NEW_PASSWORD }

    private ResetState currentState = ResetState.NONE;
    private String recoveryUsername;
    private String recoveryEmail;
    private String recoveryAnswer;

    public LoginMenu(GameContext ctx) {
        super(ctx, MenuType.LOGIN);
        this.um = UserManager.getInstance();
        this.name = "Login menu";
    }

    public String login (String username,  String password,String stayLoggedIn){
        GameClient client = GameClient.get();
        if (client.isConnected()) {
            NetResponse response = client.login(username, password);
            if (!response.ok) {
                if (um.doesUserExist(username) && um.isPasswordCorrect(password, username)) {
                    User local = um.findUserByName(username);
                    NetResponse migrate = client.register(
                        local.getUsername(),
                        password,
                        local.getNickName(),
                        local.getEmail(),
                        local.getGender() == null ? "male" : local.getGender().name().toLowerCase()
                    );
                    if (!migrate.ok && !migrate.isOffline()) {
                        return response.message;
                    }
                    client.login(username, password);
                    client.syncProfile(local);
                    um.login(local);
                } else {
                    return response.message;
                }
            } else {
                UserSnapshot snap = UserSnapshot.fromWire(response.payload);
                User user = client.applyLoginSnapshot(snap);
                applyStayLoggedIn(user, stayLoggedIn);
                DataManager.getInstance().saveUser();
                return "Logged in successfully.";
            }
        } else {
            if (!um.doesUserExist(username)) return "User does not exist!";
            if (!um.isPasswordCorrect(password, username)) return "Incorrect password.";
            um.login(um.findUserByName(username));
        }

        User user = um.getCurrentUser();
        applyStayLoggedIn(user, stayLoggedIn);
        return "Logged in successfully.";
    }

    private void applyStayLoggedIn(User user, String stayLoggedIn) {
        if (user == null) {
            return;
        }
        user.setStayedLogin(stayLoggedIn != null);
    }

    public String answerSecurityQuestion(String answer) {
        if (currentState != ResetState.AWAITING_SECURITY_ANSWER) {
            return "You are not in the password recovery process.";
        }

        if (answer == null || answer.isBlank()) {
            return "Security answer cannot be empty.";
        }

        NetResponse response =
            GameClient.get().forgotPasswordAnswer(
                recoveryUsername,
                recoveryEmail,
                answer
            );

        if (!response.ok) {
            resetRecovery();
            return response.message;
        }

        recoveryAnswer = answer;
        currentState = ResetState.AWAITING_NEW_PASSWORD;

        return "Answer is correct! Please enter your new password.";
    }

    public String updatePassword(String newPassword) {
        if (currentState != ResetState.AWAITING_NEW_PASSWORD) {
            return "Error: You cannot change password right now.";
        }

        if (!um.isPasswordValid(newPassword)) {
            return "Invalid password format.";
        }

        String validation = um.isPasswordStrong(newPassword);

        if (!"ok".equals(validation)) {
            return validation;
        }

        NetResponse response =
            GameClient.get().forgotPasswordReset(
                recoveryUsername,
                recoveryEmail,
                recoveryAnswer,
                newPassword
            );

        if (!response.ok) {
            return response.message;
        }

        resetRecovery();

        return "Password changed successfully.";
    }

    public String startForgetPasswordProcess(
        String username,
        String email
    ) {
        GameClient client = GameClient.get();

        if (!client.isConnected()) {
            return "Server is unavailable.";
        }

        NetResponse response =
            client.forgotPasswordStart(username, email);

        if (!response.ok) {
            return response.message;
        }

        int questionId;

        try {
            questionId = Integer.parseInt(response.payload);
        } catch (NumberFormatException e) {
            return "Invalid security question.";
        }

        SecurityQuestions question =
            SecurityQuestions.getQuestionById(questionId);

        if (question == null) {
            return "Invalid security question.";
        }

        recoveryUsername = username;
        recoveryEmail = email;
        recoveryAnswer = null;

        currentState = ResetState.AWAITING_SECURITY_ANSWER;

        return "Security Question: "
            + question.getQuestionText()
            + "\nPlease enter your answer.";
    }

    private void resetRecovery() {
        currentState = ResetState.NONE;
        recoveryUsername = null;
        recoveryEmail = null;
        recoveryAnswer = null;
    }

}
