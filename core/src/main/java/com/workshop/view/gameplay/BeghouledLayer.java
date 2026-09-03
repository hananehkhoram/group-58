package com.workshop.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.workshop.model.MiniGame.Beghouled.BeghouledManager;
import com.workshop.model.plants.Plant;
import pvz.skin.PvzSkin;
import com.workshop.view.Screens.PlantCardActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;

public class BeghouledLayer extends Group {

    private final GameContext ctx;

    private final float gridX;
    private final float gridY;
    private final float cellWidth;
    private final float cellHeight;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private ArrayList<Image> matchIcons = new ArrayList<>();

    private int lastMatchCount = 0;
    private Table upgradePanel;

    private final Skin skin =
        PvzSkin.get();

    private static TextureBank textureBank;
    private static PamPlayer pamPlayer;

    private int lastDisplayedSun =
        Integer.MIN_VALUE;

    public BeghouledLayer(
        GameContext ctx,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight

    ){

        this.ctx = ctx;

        initPvzLibrary();

        this.gridX = gridX;
        this.gridY = gridY;

        int columns = ctx.getLevel().getColumns();
        int rows = ctx.getLevel().getRows();

        this.cellWidth = gridWidth / columns;
        this.cellHeight = gridHeight / rows;


        setBounds(
            gridX,
            gridY,
            gridWidth,
            gridHeight
        );

        createMatchIcons();
        createUpgradePanel();


        addListener(new ClickListener(){

            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y
            ){

                int col =
                    (int)(x / cellWidth);

                int row =
                    ctx.getLevel().getRows()
                        - 1
                        - (int)(y / cellHeight);


                if(selectedRow == -1){

                    selectedRow = row;
                    selectedCol = col;

                } else {

                    selectedRow = -1;
                    selectedCol = -1;
                }
            }
        });
    }

    @Override
    public void draw(
        com.badlogic.gdx.graphics.g2d.Batch batch,
        float parentAlpha
    ){
        if (lastDisplayedSun != ctx.getSunAmount()) {

            lastDisplayedSun =
                ctx.getSunAmount();

            rebuildUpgradePanel();
        }

        int current =
            ctx.getBeghouldManager()
                .getCurrentMatches();

        while(current > lastMatchCount){

            if(!matchIcons.isEmpty()){

                Image icon =
                    matchIcons.remove(
                        matchIcons.size() - 1
                    );

                icon.remove();
            }

            lastMatchCount++;
        }


        if(selectedRow != -1 && selectedCol != -1){

            batch.end();

            shapeRenderer.setProjectionMatrix(
                getStage()
                    .getCamera()
                    .combined
            );


            shapeRenderer.begin(
                ShapeRenderer.ShapeType.Line
            );


            shapeRenderer.setColor(
                Color.YELLOW
            );


            shapeRenderer.rect(
                gridX + selectedCol * cellWidth,
                gridY
                    + (ctx.getLevel().getRows() - 1 - selectedRow)
                    * cellHeight,
                cellWidth,
                cellHeight
            );


            shapeRenderer.end();

            batch.begin();
        }


        super.draw(batch, parentAlpha);

    }

    private void createMatchIcons(){

        for(int i = 0; i < ctx.getBeghouldManager().getTargetMatches(); i++){

            TextureRegion region =
                Textures.regionOrNull(
                    "IMAGE_EFFECTS_PRIZE_PINATA_80S_PRIZE_PINATA_80S_113X113"
                );

            if(region == null){
                System.out.println("MATCH ICON NOT FOUND");
                continue;
            }

            Image icon = new Image(region);

            icon.setPosition(
                -500,
                getHeight() - 70 - i * 45
            );

            icon.setSize(30,30);

            addActor(icon);

            matchIcons.add(icon);
        }
    }

    private void initPvzLibrary() {

        if (textureBank == null || pamPlayer == null) {

            FileHandle assetsFolder =
                Gdx.files.internal("assets");

            textureBank =
                new TextureBank(
                    "768",
                    assetsFolder
                );

            pamPlayer =
                new PamPlayer(
                    textureBank,
                    assetsFolder
                );
        }
    }

    private void createUpgradePanel() {

        upgradePanel = new Table();

        upgradePanel.top();
        upgradePanel.left();

        upgradePanel.setSize(
            450f,
            260f
        );

        /*
         * مختصات نسبت به خود BeghouledLayer است.
         * مقدار منفی X یعنی سمت چپ زمین بازی.
         */
        upgradePanel.setPosition(
            -400f,
            400f
        );

        addActor(upgradePanel);

        rebuildUpgradePanel();
    }

    private void rebuildUpgradePanel() {

        if (upgradePanel == null) {
            return;
        }


        upgradePanel.clearChildren();


        for (
            BeghouledManager.UpgradeOption option :
            ctx.getBeghouldManager()
                .getAvailableUpgradeOptions()
        ) {

            Plant upgradePlant;

            try {

                upgradePlant =
                    ctx.getPlantFactory()
                        .create(
                            option.getToPlant()
                        );

            } catch (Exception e) {

                System.out.println(
                    "UPGRADE CARD SKIPPED: "
                        + option.getToPlant()
                );

                continue;
            }


            PlantCardActor card =
                new PlantCardActor(
                    upgradePlant,
                    pamPlayer,
                    textureBank,
                    skin,
                    PlantCardActor.Mode.UPGRADE
                );

            System.out.println(
                "UPGRADE COST = "
                    + option.getFromPlant()
                    + " -> "
                    + option.getToPlant()
                    + " : "
                    + option.getCost()
            );

            card.setCustomCost(
                option.getCost()
            );

            card.setOnClick(c -> {
                rebuildUpgradePanel();
            });


            upgradePanel.add(card)
                .size(120f, 140f)
                .pad(5f)
                .left()
                .row();
        }
    }
}
