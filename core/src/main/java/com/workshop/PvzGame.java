package com.workshop;

import com.badlogic.gdx.Game;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Screens.*;
import com.badlogic.gdx.Screen;
import com.workshop.model.GameContext;
import com.workshop.view.Screens.PauseOverlay;


/**
 * Entry point of the libGDX application (the "Game class" the console version never had).
 * Owns screen switching and the user-data load/save lifecycle, mirroring what
 * {@code GameEngineController} does for the console version.
 */
public class PvzGame extends Game {

    @Override
    public void create() {
        DataManager.getInstance().loadUser();

        for (User u : UserManager.getInstance().users) {
            if (u.isStayedLogin()) {
                UserManager.getInstance().login(u);
                setScreen(new MainScreen(this, u));
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
        }));
    }

    public void showMain() {
        setScreen(new MainScreen(this, UserManager.getInstance().getCurrentUser()));
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

    @Override
    public void dispose() {
        super.dispose();
        DataManager.getInstance().saveUser();
    }
}
