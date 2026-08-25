package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class BowlingRedLineLayer extends Actor {

    private final ShapeRenderer shapeRenderer;

    private final float x;
    private final float y;
    private final float height;


    public BowlingRedLineLayer(
        ShapeRenderer shapeRenderer,
        float x,
        float y,
        float height
    ) {
        this.shapeRenderer = shapeRenderer;
        this.x = x;
        this.y = y;
        this.height = height;

        setTouchable(null);
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {

        batch.end();

        shapeRenderer.setProjectionMatrix(
            batch.getProjectionMatrix()
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(Color.RED);

        shapeRenderer.line(
            x,
            y,
            x,
            y + height
        );

        shapeRenderer.end();

        batch.begin();
    }
}
