package com.workshop.view.Screens;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.workshop.PvzGame;
import com.workshop.controller.repository.Textures;
import com.badlogic.gdx.math.Matrix4;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;


public class LevelNodeActor extends Actor {

    private static final String PAM_FILE =
        "768/INITIAL/WORLDMAP/LEVEL_NODE_MINIGAME/LEVEL_NODE_MINIGAME.PAM";

    private float stateTime = 0;
    private String animation = "idle";

    private int miniGameId;
    private int levelId;
    private String levelName;

    private boolean locked;

    private float scale = 0.3f;
    private int miniGameType;

    public LevelNodeActor(int miniGameId, int levelId, String levelName, Listener listener){

        this.miniGameId = miniGameId;
        this.levelId = levelId;
        this.levelName = levelName;

        User currentUser = UserManager.getInstance().getCurrentUser();

        this.locked = !currentUser.isLevelUnlocked(levelName);

        Textures.getPamPlayer()
            .loadSync(PAM_FILE);

        String anim = locked ? "locked_idle" : "unlocked";

        Rectangle bounds =
            Textures.getPamPlayer()
                .bounds(PAM_FILE, anim);

        setSize(
            bounds.width * scale,
            bounds.height * scale
        );

        addListener(new ClickListener(){

            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y
            ){
                if(!locked){

                    listener.onEnterMiniGame(
                        miniGameId,
                        levelId,
                        levelName
                    );

                }
            }

        });
    }

    public interface Listener {
        void onEnterMiniGame(int miniGameId, int levelId, String levelName);
    }

    @Override
    public void act(float delta){

        super.act(delta);

        stateTime += delta;
    }

    private void drawScaled(
        Batch batch,
        float x,
        float y
    ){

        Matrix4 oldTransform =
            batch.getTransformMatrix().cpy();

        Matrix4 transform =
            new Matrix4(oldTransform);


        transform.translate(x, y, 0);

        transform.scale(
            scale,
            scale,
            1f
        );

        transform.translate(
            -x,
            -y,
            0
        );


        batch.setTransformMatrix(transform);


        String currentAnimation;

        if (locked) {
            currentAnimation = "locked_idle";
        } else {
            currentAnimation = "unlocked";
        }


        float offsetY = 0;

        if (locked) {
            offsetY = -1;
        }

        Textures.getPamPlayer().draw(
            batch,
            PAM_FILE,
            currentAnimation,
            stateTime,
            x + getWidth()/2,
            y + getHeight()/2 + offsetY,
            true
        );

        batch.setTransformMatrix(oldTransform);
    }

    @Override
    public void draw(
        Batch batch,
        float parentAlpha
    ){

        drawScaled(
            batch,
            getX(),
            getY()
        );
    }
}
