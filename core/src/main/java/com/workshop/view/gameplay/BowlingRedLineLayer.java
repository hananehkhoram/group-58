package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class BowlingRedLineLayer extends Actor {

    private final ShapeRenderer shapeRenderer;

    public BowlingRedLineLayer(
        ShapeRenderer shapeRenderer,
        float x,
        float y,
        float height
    ) {
        this.shapeRenderer = shapeRenderer;

        setPosition(x, y);
        setHeight(height);
        setWidth(4f);
        setTouchable(null);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeRenderer == null) return;

        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.setTransformMatrix(new Matrix4());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        batch.begin();
    }
}
