package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.workshop.controller.SpecialLevelManager.ConveyorBeltManager;
import com.workshop.controller.repository.Textures;
import com.workshop.model.plants.Plant;
import com.workshop.view.widgets.PlantCardActor;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * نوار کناری نقاله:
 * 1) پس‌زمینهٔ متحرک نوار
 * 2) حرکت پیوستهٔ کارت گیاهان از پایین به بالا
 * 3) انباشته شدن پشت کارت جلویی (بدون عبور از روی هم)
 */
public final class ConveyorBeltLayer extends Group implements Disposable {

    private static final float CARD_WIDTH = 108f;
    private static final float CARD_HEIGHT = 72f;
    private static final float CARD_GAP = 8f;
    private static final float SLOT_STRIDE = CARD_HEIGHT + CARD_GAP;
    private static final float PAD_TOP = 12f;
    private static final float PAD_BOTTOM = 10f;
    private static final float BELT_INSET = 5f;

    /** سرعت حرکت گیاهان روی نوار (پیکسل بر ثانیه) */
    private static final float PLANT_SPEED = 85f;

    /** سرعت اسکرول پس‌زمینهٔ نوار */
    private static final float BELT_SCROLL_SPEED = 55f;

    private final ConveyorBeltManager conveyorManager;
    private final Skin skin;
    private final List<PlantCardActor> seedBankCards;
    private final PlantCardActor.OnClick onCardClick;

    private final Map<Plant, MovingCard> cardsByPlant = new IdentityHashMap<>();

    private final Texture beltTexture;
    private final Texture pixelTexture;
    private float beltScroll;

    public ConveyorBeltLayer(
        ConveyorBeltManager conveyorManager,
        float worldHeight,
        List<PlantCardActor> seedBankCards,
        PlantCardActor.OnClick onCardClick
    ) {
        this.conveyorManager = conveyorManager;
        this.skin = PvzSkin.get();
        this.seedBankCards = seedBankCards;
        this.onCardClick = onCardClick;

        float beltWidth = 125f;
        float beltHeight = worldHeight - 40f;

        setBounds(110f, 20f, beltWidth, beltHeight);

        this.beltTexture = createBeltTexture();
        this.pixelTexture = createPixelTexture();
    }

    @Override
    public void act(float delta) {
        if (delta > 0f) {
            beltScroll += delta * BELT_SCROLL_SPEED;
        }

        syncCardsWithBelt();
        moveAndStackCards(delta);

        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawBeltBackground(batch, parentAlpha);

        batch.flush();
        if (clipBegin()) {
            super.draw(batch, parentAlpha);
            batch.flush();
            clipEnd();
        }
    }

    private void drawBeltBackground(Batch batch, float parentAlpha) {
        Color old = batch.getColor();
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();

        batch.setColor(0.12f, 0.07f, 0.03f, 0.95f * parentAlpha);
        batch.draw(pixelTexture, x, y, w, h);

        float innerX = x + BELT_INSET;
        float innerY = y + BELT_INSET;
        float innerW = w - BELT_INSET * 2f;
        float innerH = h - BELT_INSET * 2f;

        float texW = beltTexture.getWidth();
        float texH = beltTexture.getHeight();
        float v = (beltScroll % texH) / texH;

        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(
            beltTexture,
            innerX,
            innerY,
            innerW,
            innerH,
            0f,
            v,
            innerW / texW,
            v + innerH / texH
        );

        batch.setColor(0.62f, 0.48f, 0.22f, 0.9f * parentAlpha);
        batch.draw(pixelTexture, x, y, w, 3f);
        batch.draw(pixelTexture, x, y + h - 3f, w, 3f);
        batch.draw(pixelTexture, x, y, 3f, h);
        batch.draw(pixelTexture, x + w - 3f, y, 3f, h);

        batch.setColor(old);
    }

    private void syncCardsWithBelt() {
        List<Plant> belt = conveyorManager.getConveyorBelt();

        for (Plant plant : belt) {
            if (!cardsByPlant.containsKey(plant)) {
                spawnCard(plant);
            }
        }

        Iterator<Map.Entry<Plant, MovingCard>> it =
            cardsByPlant.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Plant, MovingCard> entry = it.next();
            if (!belt.contains(entry.getKey())) {
                MovingCard moving = entry.getValue();
                moving.card.remove();
                seedBankCards.remove(moving.card);
                it.remove();
            }
        }
    }

    private void spawnCard(Plant plant) {
        PlantCardActor card = new PlantCardActor(
            plant,
            Textures.getPamPlayer(),
            Textures.getInstance(),
            skin,
            PlantCardActor.Mode.GRID
        );

        card.setOnClick(onCardClick);
        card.setSize(CARD_WIDTH, CARD_HEIGHT);

        float startX = (getWidth() - CARD_WIDTH) / 2f;
        float startY = -CARD_HEIGHT;

        card.setPosition(startX, startY);
        addActor(card);

        cardsByPlant.put(plant, new MovingCard(card, startY));
        seedBankCards.add(card);
    }

    /**
     * حرکت پیوسته + انباشته شدن:
     * هر کارت به سمت جایگاه خودش در پشته می‌رود،
     * ولی از کارت جلویی رد نمی‌شود.
     */
    private void moveAndStackCards(float delta) {
        List<Plant> belt = conveyorManager.getConveyorBelt();
        float topSlotY = getHeight() - PAD_TOP - CARD_HEIGHT;

        List<MovingCard> ordered = new ArrayList<>(belt.size());
        for (Plant plant : belt) {
            MovingCard moving = cardsByPlant.get(plant);
            if (moving != null) {
                ordered.add(moving);
            }
        }

        for (int i = 0; i < ordered.size(); i++) {
            MovingCard moving = ordered.get(i);
            float stackTargetY = topSlotY - i * SLOT_STRIDE;

            float blockedY = stackTargetY;
            if (i > 0) {
                MovingCard front = ordered.get(i - 1);
                blockedY = Math.min(
                    blockedY,
                    front.y - SLOT_STRIDE
                );
            }

            float minY = PAD_BOTTOM;
            float maxY = Math.max(minY, blockedY);

            if (delta > 0f && moving.y < maxY) {
                moving.y = Math.min(
                    maxY,
                    moving.y + PLANT_SPEED * delta
                );
            } else if (moving.y > maxY) {
                moving.y = maxY;
            }

            moving.card.setY(moving.y);
            moving.card.updateAnimation(delta);
        }
    }

    private Texture createBeltTexture() {
        int w = 32;
        int h = 64;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        pixmap.setColor(0.40f, 0.26f, 0.12f, 1f);
        pixmap.fill();

        pixmap.setColor(0.27f, 0.16f, 0.07f, 1f);
        for (int row = 0; row < h; row += 16) {
            pixmap.fillRectangle(0, row, w, 7);
        }

        pixmap.setColor(0.55f, 0.38f, 0.18f, 1f);
        for (int row = 8; row < h; row += 16) {
            pixmap.fillRectangle(0, row, w, 2);
        }

        pixmap.setColor(0.70f, 0.55f, 0.28f, 1f);
        for (int row = 4; row < h; row += 16) {
            pixmap.fillRectangle(4, row, 4, 4);
            pixmap.fillRectangle(w - 8, row, 4, 4);
        }

        Texture texture = new Texture(pixmap);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return texture;
    }

    private Texture createPixelTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        beltTexture.dispose();
        pixelTexture.dispose();
    }

    private static final class MovingCard {
        final PlantCardActor card;
        float y;

        MovingCard(PlantCardActor card, float y) {
            this.card = card;
            this.y = y;
        }
    }
}
