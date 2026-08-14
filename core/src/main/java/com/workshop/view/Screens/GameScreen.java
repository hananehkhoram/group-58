package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.Textures;
import com.workshop.model.level.Level;
import com.workshop.model.season.Season;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;
import com.workshop.view.components.CurrencyHeader;

import pvz.skin.PvzSkin;

import java.util.List;

/**
 * Chapter select -> level select, the same shape as phase 1's Game menu: a flat
 * list of chapters (name, levels-unlocked count, locked/unlocked), and drilling
 * into one shows its levels the same way. No island map / fancy effects, per spec.
 */
public class GameScreen implements Screen {

    /** Hook this up to whatever screen-switching mechanism your Game class uses. */
    public interface Listener {
        void onEnterLevel(Season season, Level level);
        void onTravelMenu();
        void onBack(); // EXIT_TARGET(GAME) = MAIN
    }

    private final Stage stage;
    private final Skin skin;
    private final Listener listener;

    private Table root;
    private Table chaptersTable;
    private Table levelsTable;
    private Table levelRowsTable;
    private Label levelsTitle;
    private Cell<Actor> contentCell;
    private CurrencyHeader currencyHeader;
    private Texture backgroundTexture;

    private static final boolean TEST_UNLOCK_ALL_LEVELS = true;

    public GameScreen(Listener listener) {
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());

        build();
    }

    private void build() {
        buildBackground();
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Table panel = new Table();
        panel.pad(30);
        panel.defaults().pad(6);
        //panel.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        currencyHeader = new CurrencyHeader();
        panel.add(currencyHeader).right().padBottom(10).row();

        Label title = new Label("Chapters", skin, "big");
        title.setColor(Color.valueOf("00CED1"));
        panel.add(title).padBottom(16).row();

        buildChaptersStep();
        buildLevelsStep();
        contentCell = panel.add((Actor) chaptersTable);
        contentCell.row();

        TextButton travelMenuButton =
            new TextButton("TravelLog", skin, "default");

        travelMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) {
                    listener.onTravelMenu();
                }
            }
        });

        panel.add(travelMenuButton)
            .padTop(16)
            .width(200)
            .row();

        TextButton backButton = new TextButton("Back", skin, "brown");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });
        panel.add(backButton).padTop(16).width(200).row();

        ScrollPane scrollPane = new ScrollPane(panel, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // vertical only
        stage.setScrollFocus(scrollPane);

        root.add(scrollPane).grow().pad(20);
    }
    private void buildBackground() {
        FileHandle bgFile = Textures.assetsRoot().child("IMAGES/Menus/game/img.png");
        if (!bgFile.exists()) {
            Gdx.app.error("gameScreen", "Background not found at " + bgFile.file().getAbsolutePath());
            return;
        }

        backgroundTexture = new Texture(bgFile);
        Image background = new Image(backgroundTexture);
        background.setScaling(Scaling.fill); // cover the whole screen, cropping overflow instead of distorting
        background.setFillParent(true);
        stage.addActor(background);
    }

    private void buildChaptersStep() {
        chaptersTable = new Table();
        chaptersTable.defaults().pad(4);

        List<Season> chapters = DataManager.getInstance().seasons.getMainChapters();
        User currentUser = UserManager.getInstance().getCurrentUser();

        for (Season chapter : chapters) {
            chaptersTable.add(buildChapterRow(chapter, currentUser)).width(380).row();
        }
    }

    private Table buildChapterRow(Season chapter, User currentUser) {
        List<Level> levels = chapter.getLevels();
        int unlockedCount = TEST_UNLOCK_ALL_LEVELS
            ? levels.size()
            : countUnlocked(levels, currentUser);

        boolean chapterUnlocked =
            TEST_UNLOCK_ALL_LEVELS || unlockedCount > 0;

        Table row = new Table();
        row.defaults().pad(4);
        row.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        Label nameLabel = new Label(chapter.getName(), skin, "secondary");
        Label progressLabel = new Label(unlockedCount + "/" + levels.size() + " unlocked", skin, "secondary");

        row.add(nameLabel).left().expandX();
        row.add(progressLabel).right().padRight(10);

        if (chapterUnlocked) {
            TextButton enterButton = new TextButton("Enter", skin, "green_small");
            enterButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    showLevels(chapter);
                }
            });
            row.add(enterButton).width(90);
        } else {
            Label lockedLabel = new Label("Locked", skin, "secondary");
            lockedLabel.setColor(Color.SCARLET);
            row.add(lockedLabel).width(90).right();
        }

        return row;
    }

    private void buildLevelsStep() {
        levelsTable = new Table();
        levelsTable.defaults().pad(4);

        levelsTitle = new Label("", skin, "secondary");
        levelsTitle.setColor(Color.valueOf("00CED1"));

        TextButton backToChapters = new TextButton("Back to chapters", skin, "default");
        backToChapters.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showChapters();
            }
        });

        levelRowsTable = new Table();
        levelRowsTable.defaults().pad(4);

        levelsTable.add(levelsTitle).padBottom(8).row();
        levelsTable.add(backToChapters).padBottom(12).row();
        levelsTable.add(levelRowsTable).row();
    }

    private void showLevels(Season chapter) {
        levelsTitle.setText(chapter.getName());
        levelsTitle.setColor(Color.valueOf("00CED1"));
        levelRowsTable.clearChildren();

        User currentUser = UserManager.getInstance().getCurrentUser();
        for (Level level : chapter.getLevels()) {
            levelRowsTable.add(buildLevelRow(chapter, level, currentUser)).width(380).row();
        }

        contentCell.setActor(levelsTable);
    }

    private void showChapters() {
        contentCell.setActor(chaptersTable);
    }

    private Table buildLevelRow(Season chapter, Level level, User currentUser) {
        boolean unlocked =
            TEST_UNLOCK_ALL_LEVELS
                || (currentUser != null
                && currentUser.isLevelUnlocked(level.getName()));
        Table row = new Table();
        row.defaults().pad(4);
        row.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        Label nameLabel = new Label(level.getName(), skin, "secondary");
        row.add(nameLabel).left().expandX();

        if (unlocked) {
            TextButton playButton = new TextButton("Play", skin, "green_small");
            playButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Toast.showMission(stage, skin, com.workshop.model.level.LevelObjectives.describe(level));
                    if (listener != null) listener.onEnterLevel(chapter, level);
                }
            });
            row.add(playButton).width(90);
        } else {
            Label lockedLabel = new Label("Locked", skin, "secondary");
            lockedLabel.setColor(Color.SCARLET);
            row.add(lockedLabel).width(90).right();
        }

        return row;
    }

    private int countUnlocked(List<Level> levels, User user) {
        if (user == null) return 0;
        int count = 0;
        for (Level level : levels) {
            if (user.isLevelUnlocked(level.getName())) count++;
        }
        return count;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (currencyHeader != null) {
            currencyHeader.updateValues();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (currencyHeader != null) {
            currencyHeader.updateValues();
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
