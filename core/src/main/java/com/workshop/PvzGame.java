package com.workshop;

import com.badlogic.gdx.Game;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Screens.LoginScreen;
import com.workshop.view.Screens.RegisterScreen;
import com.workshop.view.Screens.MainMenuScreen;

/**
 * Entry point of the libGDX application (the "Game class" the console version never had).
 * Owns screen switching and the user-data load/save lifecycle, mirroring what
 * {@code GameEngineController} does for the console version.
 */
public class PvzGame extends Game {

    @Override
    public void create() {
        DataManager.getInstance().loadUser();

        // Mirrors GameEngineController: auto-login whoever has "stay logged in" set.
        for (User u : UserManager.getInstance().users) {
            if (u.isStayedLogin()) {
                UserManager.getInstance().login(u);
                showMain();
                return;
            }
        }

        showLogin();
    }

    public void showLogin() {
        setScreen(new LoginScreen(new LoginScreen.Listener() {
            @Override
            public void onLoginSuccess() {
                showMain();
            }

            @Override
            public void onSwitchToRegister() {
                showRegister();
            }

        }));
    }

    public void showRegister() {
        setScreen(new RegisterScreen(new RegisterScreen.Listener() {
            @Override
            public void onRegistrationFinished() {
                showLogin();
            }

            @Override
            public void onSwitchToLogin() {
                showLogin();
            }

            @Override
            public void onExit() {
                com.badlogic.gdx.Gdx.app.exit();
            }
        }));
    }

    public void showMain() {
        setScreen(new MainMenuScreen(new MainMenuScreen.Listener() {
            @Override
            public void onPlay() {
                // TODO: setScreen(new GameScreen(...)) once that screen exists
            }

            @Override
            public void onSettings() {
                // TODO: setScreen(new SettingsScreen(...)) once that screen exists
            }

            @Override
            public void onNews() {
                // TODO: setScreen(new NewsScreen(...)) once that screen exists
            }

            @Override
            public void onProfile() {
                // TODO: setScreen(new ProfileScreen(...)) once that screen exists
            }

            @Override
            public void onLogout() {
                UserManager.getInstance().logOut();
                showLogin();
            }
        }));
    }

    @Override
    public void dispose() {
        super.dispose();
        DataManager.getInstance().saveUser();
    }
}
