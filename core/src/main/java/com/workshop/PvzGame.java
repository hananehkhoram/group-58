package com.workshop;

import com.badlogic.gdx.Game;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Screens.*;
import com.badlogic.gdx.Screen;
import com.workshop.model.GameContext;
import com.workshop.view.Screens.PauseOverlay;

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
                showSettings();
            }

            @Override
            public void onNews() {
                showNews();
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

            @Override
            public void onTest() {
                showOldMain();
            }
        }));
    }

    public void showOldMain() {
        setScreen(new MainScreen(
            this,
            UserManager.getInstance().getCurrentUser()
        ));
    }

    public void showQuest() {
        setScreen(new QuestScreen(
            this,
            UserManager.getInstance().getCurrentUser()
        ));
    }

    public void showTravelMenu() {
        setScreen(new TravelMenuScreen(
            this,
            UserManager.getInstance().getCurrentUser()
        ));
    }

    public void showSkinTest() {
        setScreen(new SkinTestScreen(
            this,
            UserManager.getInstance().getCurrentUser()
        ));
    }

    public void showSettings() {
        setScreen(new SettingsScreen(
            this,
            UserManager.getInstance().getCurrentUser()
        ));
    }

    public void showNews() {
        setScreen(new NewsScreen(
            this,
            UserManager.getInstance().getCurrentUser()
        ));
    }

    public void showLeaderboard() {
        setScreen(new LeaderBoardScreen(this));
    }


    @Override
    public void dispose() {
        super.dispose();
        DataManager.getInstance().saveUser();
    }
}
