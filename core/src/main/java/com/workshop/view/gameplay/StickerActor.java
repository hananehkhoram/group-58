package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class StickerActor extends Actor {

    private final Texture[] frames;

    private float timer = 0f;

    private int currentFrame = 0;

    private final float frameTime = 0.15f;


    public StickerActor(Texture[] frames){

        this.frames = frames;

        setSize(
            frames[0].getWidth(),
            frames[0].getHeight()
        );
    }


    @Override
    public void act(float delta){

        super.act(delta);

        timer += delta;


        if(timer >= frameTime){

            timer = 0;

            currentFrame++;

            if(currentFrame >= frames.length){
                currentFrame = 0;
            }
        }
    }


    @Override
    public void draw(
        Batch batch,
        float parentAlpha
    ){

        batch.draw(
            frames[currentFrame],
            getX(),
            getY()
        );
    }


    public void dispose(){

        for(Texture texture : frames){
            texture.dispose();
        }
    }
}
