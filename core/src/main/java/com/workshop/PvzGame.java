package com.workshop;

import com.badlogic.gdx.Game;

import com.workshop.controller.MenuManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.level.Level;
import com.workshop.model.level.LevelType;
import com.workshop.model.season.Grave;
import com.workshop.model.season.Season;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Screens.*;
import com.badlogic.gdx.Screen;
import com.workshop.model.GameContext;
import com.workshop.view.Screens.PauseOverlay;

import com.workshop.view.Screens.LoginScreen;
import com.workshop.view.Screens.RegisterScreen;
import com.workshop.view.Screens.MainMenuScreen;

public class PvzGame extends Game {

    // null ctx here mirrors showCollection()'s `new CollectionScreen(null, ...)` -
    // this MenuManager only exists so we can reuse startBattle()'s GameContext /
    // GameEngine wiring; it isn't driving a console menu for the graphical screens.
    private final MenuManager menuManager = new MenuManager(null);

    @Override
    public void create() {
        DataManager.getInstance().loadUser();

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
            @Override
            public void onExit() {
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
                showGame();
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
                showProfile();
            }

            @Override
            public void onCollection() {
                showCollection();
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

    public void showCollection() {
        setScreen(new CollectionScreen(null, new CollectionScreen.Listener() {
            @Override
            public void onBack() {
                showMain();
            }

            @Override
            public void onNavigateToScreen(Screen screen) {
                setScreen(screen);
            }
        }));
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

    public void showProfile() {
        setScreen(new ProfileScreen(new ProfileScreen.Listener() {
            @Override public void onBack() { showMain(); }
        }));
    }

    public void showGame() {
        setScreen(new GameScreen(new GameScreen.Listener() {
            @Override
            public void onEnterLevel(Season season, Level level) {
                // reuse the same wiring EnterChapter uses for the console flow,
                // so GameContext + GameEngine are built the one way that's tested.
                menuManager.startBattle(level, season);
                GameContext ctx = menuManager.getCtx();

                if (level.getLevelType() == LevelType.CONVEYOR_BELT) {
                    // side-bar / conveyor-belt levels skip plant selection entirely,
                    // so nothing has placed graves / started the battle yet - do it here.
                    season.onLevelStart(ctx);
                    for (Grave g : season.getInitialGraves(level)) {
                        ctx.placeGrave(g, g.getRow(), g.getCol());
                    }
                    ctx.setBattleStarted(true);
                    goToBattleScreen(ctx);
                } else {
                    showPlantSelection(ctx);
                }
            }

            @Override
            public void onTravelMenu() {
                showTravelMenu();
            }

            @Override
            public void onBack() {
                showMain();
            }
        }));
    }

    public void showPlantSelection(GameContext ctx) {
        setScreen(new PlantSelectionScreen(ctx, new PlantSelectionScreen.Listener() {
            @Override
            public void onBack() {
                showGame();
            }

            @Override
            public void onStartBattle() {
                // PlantSelectionMenu.startGame() already placed graves and set
                // battleStarted=true (see PlantSelectionScreen) - just switch screens.
                goToBattleScreen(ctx);
            }
        }));
    }

    /**
     * Hands off to the actual battle screen. Both entry points - conveyor-belt
     * levels (which skip plant selection) and regular levels (after "Let's Rock")
     * - call this once graves are placed and {@code ctx.setBattleStarted(true)}
     * has run. {@code menuManager.getGameEngine()} is already wired to {@code ctx}
     * by {@code startBattle()} above, so the battle screen just needs to render it.
     */
    private void goToBattleScreen(GameContext ctx) {
        // TODO: point this at whatever Screen actually renders the battle -
        // that class wasn't in the files shared with me. Something like:
        // setScreen(new BattleScreen(ctx, menuManager.getGameEngine(), new BattleScreen.Listener() {
        //     @Override public void onBattleEnd() { showGame(); }
        // }));
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
