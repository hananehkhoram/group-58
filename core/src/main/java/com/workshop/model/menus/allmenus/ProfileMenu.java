package com.workshop.model.menus.allmenus;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.GameContext;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.user.Security;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.net.GameClient;
import com.workshop.net.NetResponse;
import com.workshop.net.UserSnapshot;

public class ProfileMenu extends BaseMenu {
    private UserManager um;
    protected User currentUser;
    public ProfileMenu(GameContext ctx) {
        super(ctx, MenuType.PROFILE);
        this.um = UserManager.getInstance();
        currentUser = um.getCurrentUser();
        this.name = "profile menu";
    }

    public String changeUsername(String newUsername) {
        if (!um.isUsernameValid(newUsername)) {
            return "Invalid username format.";
        }

        if (newUsername.equalsIgnoreCase(currentUser.getUsername())) {
            return "Username is equal to the current username";
        }

        GameClient client = GameClient.get();

        if (!client.isConnected()) {
            return "Server is unavailable.";
        }

        NetResponse response = client.updateUsername(newUsername);

        if (!response.ok) {
            return response.message == null
                ? "Failed to update username."
                : response.message;
        }

        if (response.payload != null && !response.payload.isBlank()) {
            UserSnapshot.fromWire(response.payload).applyTo(currentUser);
        }

        DataManager.getInstance().saveUser();

        return "Username successfully changed.";
    }

    public String changeNickname(String newNickname) {
        if (!um.isNickNameValid(newNickname)) return "Invalid nickname.";
        if (newNickname.equals(currentUser.getNickName())) return "Nickname is equal to the current nickname";

        GameClient client = GameClient.get();
        if (client.isConnected()) {
            NetResponse response = client.updateNickname(newNickname);
            if (!response.ok) {
                return response.message == null ? "Failed to update nickname." : response.message;
            }
            if (response.payload != null && !response.payload.isBlank()) {
                UserSnapshot.fromWire(response.payload).applyTo(currentUser);
            } else {
                currentUser.setNickName(newNickname);
            }
        } else {
            currentUser.setNickName(newNickname);
        }
        DataManager.getInstance().saveUser();
        return "Nickname successfully changed.";
    }

    public String changeEmail(String newEmail) {
        if (!um.isEmailValid(newEmail)) {
            return "Invalid email format.";
        }

        if (newEmail.equals(currentUser.getEmail())) {
            return "Email is equal to the current email";
        }

        GameClient client = GameClient.get();

        if (!client.isConnected()) {
            return "Server is unavailable.";
        }

        NetResponse response = client.updateEmail(newEmail);

        if (!response.ok) {
            return response.message == null
                ? "Failed to update email."
                : response.message;
        }

        if (response.payload != null && !response.payload.isBlank()) {
            UserSnapshot.fromWire(response.payload).applyTo(currentUser);
        }

        DataManager.getInstance().saveUser();

        return "Email successfully changed.";
    }

    public String changePassword(String oldPassword, String newPassword) {
        if (!um.isPasswordValid(newPassword)) {
            return "Invalid password format.";
        }

        String passwordValidation = um.isPasswordStrong(newPassword);
        if (!passwordValidation.equals("ok")) {
            return passwordValidation;
        }

        if (oldPassword != null && oldPassword.equals(newPassword)) {
            return "Password is equal to the current password";
        }

        GameClient client = GameClient.get();

        if (!client.isConnected()) {
            return "Server is unavailable.";
        }

        NetResponse response = client.updatePassword(
            oldPassword,
            newPassword
        );

        if (!response.ok) {
            return response.message == null
                ? "Failed to update password."
                : response.message;
        }

        currentUser.setPassword(
            Security.hashPassword(newPassword)
        );

        DataManager.getInstance().saveUser();

        return "Password successfully changed.";
    }

    public String showInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Username: ").append(currentUser.getUsername()).append("\n");
        sb.append("Nickname: ").append(currentUser.getNickName()).append("\n");
        sb.append("Games played: ").append(currentUser.getGamesPlayed()).append("\n");
        sb.append("Coins: ").append(currentUser.getCoins()).append("\n");
        sb.append("Gems: ").append(currentUser.getGems()).append("\n");
        sb.append("Levels: ").append(currentUser.getLastLevel()).append("\n");
        sb.append("Max mew point: ").append(currentUser.getMaxMewPoint()).append("\n");

        return sb.toString();
    }
}
