package com.workshop;

import com.badlogic.gdx.Game;

import com.workshop.controller.MenuManager;
import com.workshop.controller.repository.Audio;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.MiniGame.Beghouled.BeghouldGame;
import com.workshop.model.MiniGame.Zombotany.Zombotany;
import com.workshop.model.level.Level;
import com.workshop.model.season.Grave;
import com.workshop.model.season.Season;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.net.GameClient;
import com.workshop.view.Screens.*;
import com.badlogic.gdx.Screen;
import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;

import java.util.ArrayList;
import java.util.List;

import com.workshop.view.Screens.LoginScreen;
import com.workshop.view.Screens.RegisterScreen;
import com.workshop.view.Screens.MainMenuScreen;
import com.workshop.model.MiniGame.VaseGame.Vasecheccker;
import com.workshop.model.MiniGame.WallnutsGame.WallnutBowlingGame;
import com.workshop.model.MiniGame.Izambi.Izambi;


public class PvzGame extends Game {

    private final MenuManager menuManager = new MenuManager(null);

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);

        // "توی منوهای بازی music/main، وقتی بازی رو play می‌کنیم music/game" — a
        // single choke point instead of sprinkling Audio.playMusic() in every
        // show*() method, so no future screen can forget to set the right track.
        // Audio.playMusic() itself no-ops if the requested track is already playing.
        if (screen instanceof GamePlayScreen) {
            Audio.playMusic("music/game", true);
        } else {
            Audio.playMusic("music/main", true);
        }
    }

    @Override
    public void create() {
        GameClient.get().connect(GameClient.DEFAULT_HOST, GameClient.DEFAULT_PORT);
        DataManager.getInstance().loadUser();

        for (User u : UserManager.getInstance().users) {
            if (u.isStayedLogin()) {
                UserManager.getInstance().login(u);
                if (GameClient.get().isConnected()) {
                    com.workshop.net.NetResponse response =
                        GameClient.get().login(u.getUsername(), u.getPassword());
                    if (response.ok) {
                        com.workshop.net.UserSnapshot.fromWire(response.payload).applyTo(u);
                    }
                }
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
            public void onGreenHouse() {
                showGreenHouse();
            }
            @Override
            public void onShop() {
                showShop();
            }


            @Override
            public void onLogout() {
                GameClient.get().logout();
                UserManager.getInstance().logOut();
                showLogin();
            }

        }));
    }

    public void showQuest() {
        setScreen(new QuestScreen(
            this,
            UserManager.getInstance().getCurrentUser()
        ));
    }

    public void showTravelMenu() {
        setScreen(new TravelMenuScreen(
            this
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
    public void showShop() {
        setScreen(new ShopScreen(null, new ShopScreen.Listener() {
            @Override
            public void onBack() {
                showMain();
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

    public void showGamePlay(GameContext ctx) {
        setScreen(new GamePlayScreen(
            ctx,
            () -> restartGamePlay(ctx),
            this::showGame
        ));
    }

    private void restartGamePlay(GameContext oldCtx) {
        Level level = oldCtx.getLevel();
        Season season = oldCtx.getSeason();

        List<Plant> selectedPlants =
            new ArrayList<>(oldCtx.getActivePlants());

        menuManager.startBattle(level, season);

        GameContext newCtx = menuManager.getCtx();

        if (!level.skipsPlantSelection()) {
            for (Plant selectedPlant : selectedPlants) {
                Plant freshPlant =
                    newCtx.getPlantFactory().create(
                        selectedPlant.getName()
                    );

                freshPlant.setPlantFoodActive(
                    selectedPlant.isPlantFoodActive()
                );

                newCtx.getActivePlants().add(freshPlant);
            }
        }

        beginBattleWithoutSelection(newCtx);
    }

    private void beginBattleWithoutSelection(GameContext ctx) {
        Season season = ctx.getSeason();
        Level level = ctx.getLevel();

        season.onLevelStart(ctx);

        for (Grave grave : season.getInitialGraves(level)) {
            ctx.placeGrave(
                grave,
                grave.getRow(),
                grave.getCol()
            );
        }

        ctx.setBattleStarted(true);
        showGamePlay(ctx);
    }

    public void showGame() {
        setScreen(new GameScreen(new GameScreen.Listener() {
            @Override
            public void onEnterLevel(Season season, Level level) {
                menuManager.startBattle(level, season);
                GameContext ctx = menuManager.getCtx();

                if (level.skipsPlantSelection()) {
                    beginBattleWithoutSelection(ctx);
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
                showGamePlay(ctx);
            }
        }));
    }

    public void showGreenHouse() {
        setScreen(new GreenHouseScreen(null, new GreenHouseScreen.Listener() {
            @Override
            public void onBack() {
                showMain();
            }
        }));
    }

    public void showLeaderboard() {
        setScreen(new LeaderBoardScreen(this));
    }

    public void showIZombieMultiplayer() {
        setScreen(new OpponentSelectScreen(this));
    }

    public void showNetworkIzambiMatch(
        String matchId,
        String opponentUsername,
        com.workshop.model.MiniGame.Izambi.multiplayer.MatchRole yourRole,
        boolean isHost
    ) {
        setScreen(new NetworkIzambiScreen(this, matchId, opponentUsername, yourRole, isHost));
    }

    public void showCouchIzambi() {
        com.workshop.model.MiniGame.Izambi.multiplayer.CouchIzambiMatch match =
            new com.workshop.model.MiniGame.Izambi.multiplayer.CouchIzambiMatch();

        match.start(menuManager, 1);

        if (match.getIzambi() == null || match.getIzambi().getGameEngine() == null) {
            return;
        }

        GameContext ctx = match.getIzambi().getCtx();
        menuManager.setCtx(ctx);
        menuManager.setGameEngine(match.getIzambi().getGameEngine());

        setScreen(
            new CouchIzambiScreen(
                this,
                match,
                ctx,
                this::showCouchIzambi,
                this::showTravelMenu
            )
        );
    }

    public void showVaseBreaker(int levelNumber) {
        Vasecheccker vaseGame = new Vasecheccker();

        vaseGame.startMiniGame(
            menuManager,
            levelNumber
        );

        GameContext ctx = vaseGame.getCtx();

        if (ctx == null
            || vaseGame.getGameEngine() == null) {
            return;
        }

        menuManager.setCtx(ctx);
        menuManager.setGameEngine(
            vaseGame.getGameEngine()
        );

        setScreen(
            new GamePlayScreen(
                ctx,
                () -> showVaseBreaker(levelNumber),
                this::showTravelMenu
            )
        );
    }

    public void showWallnutBowling(int levelNumber) {
        WallnutBowlingGame bowlingGame =
            new WallnutBowlingGame();

        bowlingGame.start(
            menuManager,
            levelNumber
        );

        GameContext ctx = bowlingGame.getCtx();

        if (ctx == null
            || bowlingGame.getGameEngine() == null) {
            return;
        }

        menuManager.setCtx(ctx);
        menuManager.setGameEngine(
            bowlingGame.getGameEngine()
        );

        setScreen(
            new GamePlayScreen(
                ctx,
                () -> showWallnutBowling(levelNumber),
                this::showTravelMenu
            )
        );
    }

    public void showIZombie(int levelNumber) {
        Izambi iZombieGame = new Izambi();

        iZombieGame.startMiniGame(
            menuManager,
            levelNumber
        );

        GameContext ctx = iZombieGame.getCtx();

        if (ctx == null
            || iZombieGame.getGameEngine() == null) {
            return;
        }

        menuManager.setCtx(ctx);
        menuManager.setGameEngine(
            iZombieGame.getGameEngine()
        );

        setScreen(
            new GamePlayScreen(
                ctx,
                () -> showIZombie(levelNumber),
                this::showTravelMenu
            )
        );
    }

    public void showBeghouled(int levelNumber) {
        BeghouldGame beghouledGame =
            new BeghouldGame();

        beghouledGame.start(
            menuManager,
            levelNumber
        );

        GameContext ctx =
            beghouledGame.getCtx();

        if (ctx == null
            || beghouledGame.getGameEngine() == null) {
            return;
        }

        menuManager.setCtx(ctx);
        menuManager.setGameEngine(
            beghouledGame.getGameEngine()
        );

        setScreen(
            new GamePlayScreen(
                ctx,
                () -> showBeghouled(levelNumber),
                this::showTravelMenu
            )
        );
    }

    public void showZombotany(int levelNumber) {
        Zombotany zombotanyGame =
            new Zombotany();

        zombotanyGame.startLevel(
            menuManager,
            levelNumber - 1
        );

        GameContext ctx =
            zombotanyGame.getCtx();

        if (ctx == null
            || zombotanyGame.getGameEngine() == null) {
            return;
        }

        menuManager.setCtx(ctx);
        menuManager.setGameEngine(
            zombotanyGame.getGameEngine()
        );

        setScreen(
            new PlantSelectionScreen(
                ctx,
                new PlantSelectionScreen.Listener() {

                    @Override
                    public void onBack() {
                        showTravelMenu();
                    }

                    @Override
                    public void onStartBattle() {
                        setScreen(
                            new GamePlayScreen(
                                ctx,
                                () -> showZombotany(
                                    levelNumber
                                ),
                                PvzGame.this::showTravelMenu
                            )
                        );
                    }
                }
            )
        );
    }

    @Override
    public void dispose() {
        super.dispose();
        DataManager.getInstance().saveUser();
    }
}
