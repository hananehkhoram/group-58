package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.ShopMenu;
import com.workshop.model.plants.Plant;
import com.workshop.model.shopData.Currency;
import com.workshop.model.shopData.DailyOffer;
import com.workshop.model.shopData.ItemType;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;
import com.workshop.view.components.CurrencyHeader;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShopScreen implements Screen {

    public interface Listener {
        void onBack();
    }

    private static class ShopEntry {
        final int id;
        final String title;
        final int price;
        final Currency currency;
        final boolean purchased;
        final ItemType itemType;
        final Plant iconPlant;
        final boolean isDaily;

        ShopEntry(int id, String title, int price, Currency currency, boolean purchased, ItemType itemType, Plant iconPlant, boolean isDaily) {
            this.id = id;
            this.title = title;
            this.price = price;
            this.currency = currency;
            this.purchased = purchased;
            this.itemType = itemType;
            this.iconPlant = iconPlant;
            this.isDaily = isDaily;
        }
    }

    private static final float DESIGN_WIDTH = 1024f;
    private static final float DESIGN_HEIGHT = 576f;

    private final GameContext ctx;
    private final Listener listener;
    private final Stage stage;
    private final ScreenViewport viewport;
    private final Skin skin;

    private ShopMenu shopMenu;
    private User currentUser;
    private Table itemsGrid;
    private CurrencyHeader currencyHeader;

    private Texture menuBgTexture, overlayTexture, panelTexture;
    private Drawable cardHeaderBg, cardBodyBg, grassIconBg, redRibbonBg;

    private TextureBank textureBank;
    private PamPlayer pamPlayer;
    private float stateTime = 0f;
    private float conversionPurchasedTime = -10f;

    private List<Label> dailyTimerLabels = new ArrayList<>();

    public ShopScreen(GameContext ctx, Listener listener) {
        this.ctx = ctx;
        this.listener = listener;
        this.skin = PvzSkin.get();

        enableLinearFilteringOnFonts();

        this.viewport = new ScreenViewport();
        this.stage = new Stage(viewport);
        this.shopMenu = new ShopMenu(ctx);

        initPvzLibrary();
        initCustomDrawables();
        buildUI();
    }

    private void enableLinearFilteringOnFonts() {
        if (skin == null) return;
        for (BitmapFont font : skin.getAll(BitmapFont.class).values()) {
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            font.getData().setScale(1.25f);
        }
    }

    private void initPvzLibrary() {
        if (CollectionScreen.textureBank == null || CollectionScreen.pamPlayer == null) {
            var assetsFolder = Gdx.files.internal("assets");
            CollectionScreen.textureBank = new TextureBank("768", assetsFolder);
            CollectionScreen.pamPlayer = new PamPlayer(CollectionScreen.textureBank, assetsFolder);
        }
        this.textureBank = CollectionScreen.textureBank;
        this.pamPlayer = CollectionScreen.pamPlayer;
    }

    private void initCustomDrawables() {
        cardHeaderBg = createColorDrawable(Color.valueOf("4C9A2A"));
        cardBodyBg = createColorDrawable(Color.valueOf("F3E5AB"));
        grassIconBg = createColorDrawable(Color.valueOf("6B8E23"));
        redRibbonBg = createColorDrawable(new Color(0.85f, 0.15f, 0.15f, 0.95f));
    }

    private Drawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private void buildUI() {
        currentUser = UserManager.getInstance().getCurrentUser();

        stage.clear();
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        // بارگذاری پس‌زمینه از مسیر IMAGES/Menus/shop/shop_bg.png
        if (menuBgTexture == null) {
            menuBgTexture = new Texture(Gdx.files.internal("IMAGES/Menus/shop/shop_bg.png"));
            menuBgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(menuBgTexture)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);

        stage.addActor(bg);
        stage.addActor(rootTable);

        Table header = new Table();
        ImageButton closeBtn = new ImageButton(skin, "generic_close_circle");
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });

        Label titleLabel = createSafeLabel("CRAZY DAVE'S SHOP", "big");
        titleLabel.setColor(Color.GOLD);

        currencyHeader = new CurrencyHeader();

        header.add(closeBtn).size(45, 45).left().pad(10);
        header.add(titleLabel).expandX().center();
        header.add(currencyHeader).right().padRight(15);

        rootTable.add(header).fillX().padTop(5).row();
        rootTable.add(buildDivider()).fillX().height(3).padLeft(15).padRight(15).padBottom(5).row();

        itemsGrid = new Table().center();
        ScrollPane scrollPane = new ScrollPane(itemsGrid, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, true);

        rootTable.add(scrollPane).grow().pad(10).row();
        refreshItemsGrid();
    }

    private void refreshItemsGrid() {
        itemsGrid.clear();
        dailyTimerLabels.clear();
        currentUser = UserManager.getInstance().getCurrentUser();

        for (ShopEntry entry : buildEntries()) {
            itemsGrid.add(createItemCard(entry)).width(250f).height(480f).pad(15);
        }
    }

    private List<ShopEntry> buildEntries() {
        List<ShopEntry> entries = new ArrayList<>();
        DailyOffer offer = shopMenu.getShop().getDailyOffer();

        if (offer != null) {
            String plantName = offer.getPlantType() != null ? offer.getPlantType().getName() : offer.getName();
            entries.add(new ShopEntry(0, "Daily: " + plantName, offer.getPrice(), offer.getCurrency(), offer.isPurchased(), null, offer.getPlantType(), true));
        }

        for (ItemType item : ItemType.values()) {
            entries.add(new ShopEntry(item.getId(), item.getDisplayName(), item.getPrice(), item.getCurrency(), false, item, null, false));
        }
        return entries;
    }

    private Table createItemCard(ShopEntry entry) {
        Table card = new Table().top();

        Table headerTable = new Table();
        headerTable.setBackground(cardHeaderBg);
        Label nameLbl = createSafeLabel(entry.title, "default");
        nameLbl.setColor(Color.WHITE);
        nameLbl.setAlignment(Align.center);
        nameLbl.setWrap(true);
        headerTable.add(nameLbl).width(230f).pad(8).center();

        Table bodyTable = new Table().top().pad(10);
        bodyTable.setBackground(cardBodyBg);

        Stack iconStack = new Stack();

        Table iconHolder = new Table();
        iconHolder.setBackground(grassIconBg);
        iconHolder.add(buildIcon(entry)).size(180f, 180f);

        iconStack.add(iconHolder);

        if (entry.isDaily) {
            WidgetGroup ribbon = createDiagonalRibbon();

            float offsetX = 140f;
            float offsetY = 100f;

            Container<WidgetGroup> ribbonContainer = new Container<>(ribbon);
            ribbonContainer.top().center();
            ribbonContainer.padTop(-offsetY).padLeft(-offsetX);

            iconStack.add(ribbonContainer);
        }

        Table priceTable = new Table().center();
        Label priceLbl = createSafeLabel(String.valueOf(entry.price), "default");
        priceLbl.setColor(entry.currency == Currency.COIN ? Color.valueOf("8B5A00") : Color.valueOf("005F73"));

        priceTable.add(priceLbl).padRight(10);
        priceTable.add(buildCurrencyIcon(entry.currency)).size(36, 36);

        TextButton buyBtn = new TextButton(entry.purchased ? "Sold" : "Buy", skin, "green_small");
        buyBtn.setDisabled(entry.purchased);
        buyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onBuyPressed(entry);
            }
        });

        bodyTable.add(iconStack).size(190f, 190f).padTop(10).padBottom(15).row();
        bodyTable.add(priceTable).expandY().center().padBottom(15).row();
        bodyTable.add(buyBtn).width(180f).height(50f).bottom().padBottom(10);

        card.add(headerTable).fillX().height(60f).row();
        card.add(bodyTable).grow();

        return card;
    }

    private WidgetGroup createDiagonalRibbon() {
        WidgetGroup group = new WidgetGroup();

        float width = 120f;
        float height = 24f;

        Image bg = new Image(redRibbonBg);
        bg.setSize(width, height);

        Label timerLbl = createSafeLabel(getRemainingTimeString(), "default");
        timerLbl.setColor(Color.WHITE);
        timerLbl.setAlignment(Align.center);

        dailyTimerLabels.add(timerLbl);

        Actor clockIcon = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                renderPamAnimation(batch, "768/FULL/UI/PENNY_PURSUITS/ZOMBOSS/CLOCK_ICON/CLOCK_ICON.PAM", "default", getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0.25f, stateTime);
            }
        };

        Table contentTable = new Table();
        contentTable.setSize(width, height);
        contentTable.add(timerLbl).expandX().center().padLeft(6f);
        contentTable.add(clockIcon).size(18f, 18f).padRight(6f);

        group.addActor(bg);
        group.addActor(contentTable);
        group.setSize(width, height);

        return group;
    }

    private String getRemainingTimeString() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT);
        Duration duration = Duration.between(now, nextMidnight);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateDailyTimers() {
        if (dailyTimerLabels.isEmpty()) return;
        String timeStr = getRemainingTimeString();
        for (Label lbl : dailyTimerLabels) {
            lbl.setText(timeStr);
        }
    }

    private Actor buildCurrencyIcon(Currency currency) {
        return new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                if (currency == Currency.COIN) {
                    renderPamAnimation(batch, "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM", "animation", getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0.25f, stateTime);
                } else {
                    renderPamAnimation(batch, "768/INITIAL/EFFECTS/COIN_DIAMOND/COIN_DIAMOND.PAM", "idle", getX() + getWidth() / 2f + 4f, getY() + getHeight() / 2f, 0.16f, stateTime);
                }
            }
        };
    }

    private Actor buildIcon(ShopEntry entry) {
        return new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                if (entry.itemType == ItemType.CURRENCY_CONVERSION) {
                    float durationSincePurchase = stateTime - conversionPurchasedTime;
                    String clip = (durationSincePurchase >= 0f && durationSincePurchase < 1.2f) ? "born" : "idle";
                    float animTime = (clip.equals("born")) ? durationSincePurchase : stateTime;

                    renderPamAnimation(batch, "768/INITIAL/EFFECTS/PRIZE_COINS_LARGE/PRIZE_COINS_LARGE.PAM", clip, getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0.35f, animTime);
                } else if (entry.itemType == ItemType.PLANT_FOOD) {
                    renderPamAnimation(batch, "768/INITIAL/EFFECTS/PLANTFOOD_PICKUP/PLANTFOOD_PICKUP.PAM", "idle", getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0.45f, stateTime);
                } else if (entry.itemType == ItemType.POT_UNLOCK) {
                    renderPamAnimation(batch, "768/INITIAL/ZEN_GARDEN/GROWING_PLANT_SLOT/GROWING_PLANT_SLOT.PAM", "idle", getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0.45f, stateTime);
                } else if (entry.itemType == ItemType.SELECTED_SEED_PACK || entry.itemType == ItemType.RANDOM_SEED_PACK) {
                    String clip = (entry.itemType == ItemType.SELECTED_SEED_PACK) ? "sunflower_idle" : "sunflower_shout";
                    renderPamAnimation(batch, "768/FULL/NPC/SUNFLOWER/SUNFLOWER.PAM", clip, getX() + getWidth() / 2f, getY() + 65f, 0.18f, stateTime);
                } else if (entry.iconPlant != null) {
                    String rawName = entry.iconPlant.getName().toUpperCase().replace(" ", "").replace("-", "");
                    renderPamAnimation(batch, "PLANT/" + rawName + "/" + rawName + ".PAM", "idle", getX() + getWidth() / 2f, getY() + getHeight() / 2f - 10f, 0.45f, stateTime);
                }
            }
        };
    }

    private void renderPamAnimation(Batch batch, String path, String clip, float x, float y, float scale, float time) {
        if (textureBank != null) textureBank.update();
        Matrix4 oldTransform = batch.getTransformMatrix().cpy();

        Matrix4 transform = new Matrix4(oldTransform);
        transform.translate(x, y, 0);
        transform.scale(scale, scale, 1f);
        transform.translate(-x, -y, 0);
        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(batch, path, clip, time, x, y, true);
        } catch (Exception ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    private void onBuyPressed(ShopEntry entry) {
        if (entry.itemType == ItemType.SELECTED_SEED_PACK) {
            showPlantPickerOverlay(entry);
            return;
        }

        String error = shopMenu.canPurchase(entry.id, 1);
        if (error != null) {
            Toast.showError(stage, skin, error);
            return;
        }

        showConfirmationOverlay(entry.title, () -> completePurchase(entry, null));
    }

    private Table buildOverlayRoot(Table panel) {
        Table overlayRoot = new Table();
        overlayRoot.setFillParent(true);

        if (overlayTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 0.7f);
            pixmap.fill();
            overlayTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        overlayRoot.setBackground(new TextureRegionDrawable(new TextureRegion(overlayTexture)));
        overlayRoot.add(panel);
        return overlayRoot;
    }

    private Table buildPanel() {
        if (panelTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(0.22f, 0.15f, 0.1f, 0.98f);
            pixmap.fill();
            panelTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        Table panel = new Table();
        panel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        panel.pad(25);
        return panel;
    }

    private void showConfirmationOverlay(String itemDescription, Runnable onConfirm) {
        Table panel = buildPanel();

        Label titleLbl = createSafeLabel("Purchase Confirmation", "big");
        titleLbl.setColor(Color.GOLD);

        Label question = createSafeLabel("Would you like to purchase " + itemDescription + "?", "default");
        question.setWrap(true);
        question.setAlignment(Align.center);

        TextButton yesBtn = new TextButton("Yes", skin, "green_small");
        TextButton noBtn = new TextButton("No", skin, "brown");

        Table overlayRoot = buildOverlayRoot(panel);

        yesBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                overlayRoot.remove();
                onConfirm.run();
            }
        });
        noBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                overlayRoot.remove();
            }
        });

        Table buttonRow = new Table();
        buttonRow.add(yesBtn).width(120).height(45).padRight(15);
        buttonRow.add(noBtn).width(120).height(45);

        panel.add(titleLbl).padBottom(15).row();
        panel.add(question).width(320).padBottom(20).row();
        panel.add(buttonRow);

        stage.addActor(overlayRoot);
    }

    private void showPlantPickerOverlay(ShopEntry entry) {
        List<Plant> unlockedPlants = currentUser != null ? currentUser.getUnlockedPlantTypes() : null;
        if (unlockedPlants == null || unlockedPlants.isEmpty()) {
            Toast.showError(stage, skin, "You have no unlocked plants to buy seeds for!");
            return;
        }

        String error = shopMenu.canPurchase(entry.id, 1);
        if (error != null) {
            Toast.showError(stage, skin, error);
            return;
        }

        Table panel = buildPanel();
        Label titleLbl = createSafeLabel("Choose a plant", "big");
        titleLbl.setColor(Color.GOLD);
        panel.add(titleLbl).padBottom(15).row();

        Table list = new Table();
        Table overlayRoot = buildOverlayRoot(panel);

        for (Plant plant : unlockedPlants) {
            TextButton plantBtn = new TextButton(plant.getName(), skin, "purple");
            plantBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    overlayRoot.remove();
                    showConfirmationOverlay(entry.title + " (" + plant.getName() + ")", () -> completePurchase(entry, plant.getName()));
                }
            });
            list.add(plantBtn).width(220).height(40).pad(4).row();
        }

        ScrollPane scrollPane = new ScrollPane(list, skin);
        scrollPane.setScrollingDisabled(true, false);
        panel.add(scrollPane).width(240).height(220).padBottom(15).row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                overlayRoot.remove();
            }
        });

        panel.add(cancelBtn).width(150).height(40);
        stage.addActor(overlayRoot);
    }

    private void completePurchase(ShopEntry entry, String plantType) {
        String result = (entry.id == 0) ? shopMenu.buyDailyOffer(1) : shopMenu.buyItem(entry.id, 1, plantType);

        if (result != null && result.startsWith("Successfully")) {
            Toast.showSuccess(stage, skin, result);
            if (entry.itemType == ItemType.CURRENCY_CONVERSION) {
                conversionPurchasedTime = stateTime;
            }
        } else {
            Toast.showError(stage, skin, result);
        }

        currencyHeader.updateValues();
        refreshItemsGrid();
    }

    private Table buildDivider() {
        Table divider = new Table();
        if (skin.has("image_ui_almanac_general_line_10", Drawable.class)) {
            divider.setBackground(skin.getDrawable("image_ui_almanac_general_line_10"));
        } else {
            divider.setBackground(createColorDrawable(new Color(0.6f, 0.4f, 0.2f, 0.8f)));
        }
        return divider;
    }

    private Label createSafeLabel(String text, String styleName) {
        return skin.has(styleName, Label.LabelStyle.class) ? new Label(text, skin, styleName) : new Label(text, skin);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        refreshItemsGrid();
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        updateDailyTimers();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        float scale = Math.min((float) width / DESIGN_WIDTH, (float) height / DESIGN_HEIGHT);
        viewport.setUnitsPerPixel(1f / scale);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (menuBgTexture != null) menuBgTexture.dispose();
        if (overlayTexture != null) overlayTexture.dispose();
        if (panelTexture != null) panelTexture.dispose();
    }
}
