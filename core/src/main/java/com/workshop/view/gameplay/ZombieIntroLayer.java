package com.workshop.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;

public final class ZombieIntroLayer extends Group {

    private final ZombieAnimationResolver resolver;

    public ZombieIntroLayer() {
        resolver = new ZombieAnimationResolver();
    }

    public boolean addZombie(
        String pamName,
        float x,
        float y
    ) {
        ZombieAnimationSpec spec =
            resolver.resolve(pamName);

        if (spec == null) {
            return false;
        }

        ZombiePreviewActor actor =
            new ZombiePreviewActor(
                spec,
                Textures.getPamPlayer()
            );

        actor.setPosition(x, y);
        addActor(actor);

        return true;
    }
}
