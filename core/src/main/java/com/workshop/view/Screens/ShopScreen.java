package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        ShopEntry(int id, String title, int price, Currency currency, boolean purchased, ItemType itemType, Plant iconPlant) {
            this.id = id;
            this.title = title;
            this.price = price;
            this.currency = currency;
            this.purchased = purchased;
            this.itemType = itemType;
            this.iconPlant = iconPlant;
        }

        boolean isCurrencyConversion() {
            return itemType == ItemType.CURRENCY_CONVERSION;
        }

        boolean isPlantFood() {
            return itemType == ItemType.PLANT_FOOD;
        }

        boolean isPotUnlock() {
            return itemType == ItemType.POT_UNLOCK;
        }

        boolean isSeedPacket() {
            return itemType == ItemType.SELECTED_SEED_PACK;
        }

        boolean isRandomSeedPacket() {
            return itemType == ItemType.RANDOM_SEED_PACK;
        }
    }

    private static final float VIRTUAL_WIDTH = 1024f;
    private static final float VIRTUAL_HEIGHT = 576f;

    private final GameContext ctx;
    private final Listener listener;

    private final Stage stage;
    private final Skin skin;
    private ShopMenu shopMenu;
    private User currentUser;

    private Table rootTable;
    private Table itemsGrid;
    private CurrencyHeader currencyHeader;
    private Texture menuBgTexture;
    private Texture overlayTexture;
    private Texture panelTexture;

    private Drawable cardHeaderBg;
    private Drawable cardBodyBg;
    private Drawable grassIconBg;

    private TextureBank textureBank;
    private PamPlayer pamPlayer;
    private float stateTime = 0f;

    private final Map<Integer, String> currencyAnimState = new HashMap<>();
    private final Map<Integer, Float> currencyAnimStartTime = new HashMap<>();

    public ShopScreen(GameContext ctx, Listener listener) {
        this.ctx = ctx;
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

        initPvzLibrary();
        initCustomDrawables();
        buildUI();
    }

    private void initPvzLibrary() {
        if (CollectionScreen.textureBank == null || CollectionScreen.pamPlayer == null) {
            com.badlogic.gdx.files.FileHandle assetsFolder = Gdx.files.internal("assets");
            CollectionScreen.textureBank = new TextureBank("768", assetsFolder);
            CollectionScreen.pamPlayer = new PamPlayer(CollectionScreen.textureBank, assetsFolder);
        }
        this.textureBank = CollectionScreen.textureBank;
        this.pamPlayer = CollectionScreen.pamPlayer;
    }

    private void initCustomDrawables() {
        Pixmap pHeader = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pHeader.setColor(Color.valueOf("4C9A2A"));
        pHeader.fill();
        cardHeaderBg = new TextureRegionDrawable(new TextureRegion(new Texture(pHeader)));
        pHeader.dispose();

        Pixmap pBody = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pBody.setColor(Color.valueOf("F3E5AB"));
        pBody.fill();
        cardBodyBg = new TextureRegionDrawable(new TextureRegion(new Texture(pBody)));
        pBody.dispose();

        Pixmap pGrass = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pGrass.setColor(Color.valueOf("6B8E23"));
        pGrass.fill();
        grassIconBg = new TextureRegionDrawable(new TextureRegion(new Texture(pGrass)));
        pGrass.dispose();
    }

    private void buildUI() {
        shopMenu = new ShopMenu(ctx);
        currentUser = UserManager.getInstance().getCurrentUser();

        stage.clear();
        rootTable = new Table();
        rootTable.setFillParent(true);

        if (menuBgTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.valueOf("2D1E18"));
            pixmap.fill();
            menuBgTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(menuBgTexture)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        stage.addActor(rootTable);

        Table header = new Table();
        ImageButton closeButton = new ImageButton(skin, "generic_close_circle");
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });
        header.add(closeButton).size(45, 45).left().pad(10);

        Label titleLabel = createSafeLabel("CRAZY DAVE'S SHOP", "big");
        titleLabel.setFontScale(0.85f);
        titleLabel.setColor(Color.GOLD);
        header.add(titleLabel).expandX().center();

        currencyHeader = new CurrencyHeader();
        header.add(currencyHeader).right().padRight(15);
        rootTable.add(header).fillX().padTop(5).row();

        Table divider = buildDivider();
        rootTable.add(divider).fillX().height(3).padLeft(15).padRight(15).padBottom(5).row();

        itemsGrid = new Table();
        itemsGrid.top();

        ScrollPane scrollPane = new ScrollPane(itemsGrid, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, true); // اسکرول افقی فعال
        rootTable.add(scrollPane).grow().pad(10).row();

        refreshItemsGrid();
    }

    private List<ShopEntry> buildEntries() {
        List<ShopEntry> entries = new ArrayList<>();

        DailyOffer offer = currentUser != null ? currentUser.getLastDailyOffer() : null;
        if (offer != null) {
            String plantName = offer.getPlantType() != null ? offer.getPlantType().getName() : offer.getName();
            String title = "Daily: " + plantName;
            entries.add(new ShopEntry(0, title, offer.getPrice(), offer.getCurrency(), offer.isPurchased(), null, offer.getPlantType()));
        }

        for (ItemType item : ItemType.values()) {
            entries.add(new ShopEntry(item.getId(), item.getDisplayName(), item.getPrice(), item.getCurrency(), false, item, null));
        }

        return entries;
    }

    private void refreshItemsGrid() {
        itemsGrid.clear();
        currentUser = UserManager.getInstance().getCurrentUser();

        for (ShopEntry entry : buildEntries()) {
            Table card = createItemCard(entry);
            // ارتفاع کارت‌ها به ۴۱۰ افزایش یافت تا تمام ستون عمودی را پر کند
            itemsGrid.add(card).size(210, 410).pad(10);
        }
    }

    private Table createItemCard(ShopEntry entry) {
        Table card = new Table();
        card.top();

        // هدر کارت
        Table headerTable = new Table();
        headerTable.setBackground(cardHeaderBg);
        Label nameLbl = createSafeLabel(entry.title, "default");
        nameLbl.setFontScale(0.75f);
        nameLbl.setColor(Color.WHITE);
        nameLbl.setAlignment(Align.center);
        nameLbl.setWrap(true);
        headerTable.add(nameLbl).width(190).pad(4).center();
        card.add(headerTable).fillX().height(45).row();

        // بدنه اصلی کارت
        Table bodyTable = new Table();
        bodyTable.setBackground(cardBodyBg);
        bodyTable.top().pad(10);

        // باکس تصویر/انیمیشن
        Table iconHolder = new Table();
        iconHolder.setBackground(grassIconBg);
        iconHolder.add(buildIcon(entry)).size(130, 130);
        bodyTable.add(iconHolder).size(150, 150).padTop(10).padBottom(20).row();

        // بخش قیمت
        Table priceTable = new Table();
        priceTable.center();

        Label priceLbl = createSafeLabel(String.valueOf(entry.price), "default");
        priceLbl.setFontScale(0.9f);
        priceLbl.setColor(entry.currency == Currency.COIN ? Color.valueOf("8B5A00") : Color.valueOf("005F73"));

        priceTable.add(priceLbl).padRight(10);
        priceTable.add(buildCurrencyIcon(entry.currency)).size(24, 24);
        bodyTable.add(priceTable).expandY().center().padBottom(15).row();

        // دکمه خرید
        TextButton buyBtn = new TextButton(entry.purchased ? "Sold" : "Buy", skin, "green_small");
        buyBtn.setDisabled(entry.purchased);
        buyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onBuyPressed(entry);
            }
        });
        bodyTable.add(buyBtn).width(140).height(42).bottom().padBottom(15);

        card.add(bodyTable).grow();

        return card;
    }

    private Actor buildCurrencyIcon(Currency currency) {
        return new Table() {
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (textureBank != null) textureBank.update();

                String pamPath;
                String clipName;

                if (currency == Currency.COIN) {
                    pamPath = "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM";
                    clipName = "animation";
                } else {
                    pamPath = "768/INITIAL/EFFECTS/COIN_DIAMOND/COIN_DIAMOND.PAM";
                    clipName = "idle";
                }

                float drawX = getX() + getWidth() / 2f + 15f;
                float drawY = getY() + getHeight() / 2f;

                Matrix4 oldTransform = batch.getTransformMatrix().cpy();
                float scale = 0.22f;
                Matrix4 transform = new Matrix4(oldTransform);
                transform.translate(drawX, drawY, 0);
                transform.scale(scale, scale, 1f);
                transform.translate(-drawX, -drawY, 0);
                batch.setTransformMatrix(transform);

                try {
                    pamPlayer.draw(batch, pamPath, clipName, stateTime, drawX, drawY, true);
                } catch (Exception ignored) {
                }

                batch.setTransformMatrix(oldTransform);
            }
        };
    }

    private Actor buildIcon(ShopEntry entry) {
        if (entry.isCurrencyConversion()) {
            return new Table() {
                @Override
                public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                    super.draw(batch, parentAlpha);
                    if (textureBank != null) textureBank.update();

                    String pamPath = "768/INITIAL/EFFECTS/PRIZE_COINS_LARGE/PRIZE_COINS_LARGE.PAM";
                    String currentClip = currencyAnimState.getOrDefault(entry.id, "idle");
                    float startTime = currencyAnimStartTime.getOrDefault(entry.id, 0f);
                    float animTime = stateTime - startTime;

                    float drawX = getX() + getWidth() / 2f;
                    float drawY = getY() + getHeight() / 2f;

                    Matrix4 oldTransform = batch.getTransformMatrix().cpy();
                    float scale = 0.35f;
                    Matrix4 transform = new Matrix4(oldTransform);
                    transform.translate(drawX, drawY, 0);
                    transform.scale(scale, scale, 1f);
                    transform.translate(-drawX, -drawY, 0);
                    batch.setTransformMatrix(transform);

                    try {
                        pamPlayer.draw(batch, pamPath, currentClip, animTime, drawX, drawY, true);
                    } catch (Exception ignored) {
                    }

                    batch.setTransformMatrix(oldTransform);
                }
            };
        }

        if (entry.isPlantFood()) {
            return new Table() {
                @Override
                public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                    super.draw(batch, parentAlpha);
                    if (textureBank != null) textureBank.update();

                    String pamPath = "768/INITIAL/EFFECTS/PLANTFOOD_PICKUP/PLANTFOOD_PICKUP.PAM";
                    float drawX = getX() + getWidth() / 2f;
                    float drawY = getY() + getHeight() / 2f;

                    Matrix4 oldTransform = batch.getTransformMatrix().cpy();
                    float scale = 0.45f;
                    Matrix4 transform = new Matrix4(oldTransform);
                    transform.translate(drawX, drawY, 0);
                    transform.scale(scale, scale, 1f);
                    transform.translate(-drawX, -drawY, 0);
                    batch.setTransformMatrix(transform);

                    try {
                        pamPlayer.draw(batch, pamPath, "idle", stateTime, drawX, drawY, true);
                    } catch (Exception ignored) {
                    }

                    batch.setTransformMatrix(oldTransform);
                }
            };
        }

        if (entry.isPotUnlock()) {
            return new Table() {
                @Override
                public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                    super.draw(batch, parentAlpha);
                    if (textureBank != null) textureBank.update();

                    String pamPath = "768/INITIAL/ZEN_GARDEN/GROWING_PLANT_SLOT/GROWING_PLANT_SLOT.PAM";
                    float drawX = getX() + getWidth() / 2f;
                    float drawY = getY() + getHeight() / 2f;

                    Matrix4 oldTransform = batch.getTransformMatrix().cpy();
                    float scale = 0.45f;
                    Matrix4 transform = new Matrix4(oldTransform);
                    transform.translate(drawX, drawY, 0);
                    transform.scale(scale, scale, 1f);
                    transform.translate(-drawX, -drawY, 0);
                    batch.setTransformMatrix(transform);

                    try {
                        pamPlayer.draw(batch, pamPath, "idle", stateTime, drawX, drawY, true);
                    } catch (Exception ignored) {
                    }

                    batch.setTransformMatrix(oldTransform);
                }
            };
        }

        if (entry.isSeedPacket() || entry.isRandomSeedPacket()) {
            return new Table() {
                @Override
                public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                    super.draw(batch, parentAlpha);
                    if (textureBank != null) textureBank.update();

                    String pamPath = "768/FULL/NPC/SUNFLOWER/SUNFLOWER.PAM";
                    String clipName = entry.isSeedPacket() ? "sunflower_idle" : "sunflower_shout";

                    float drawX = getX() + getWidth() / 2f;
                    float drawY = getY() + 10f;

                    Matrix4 oldTransform = batch.getTransformMatrix().cpy();
                    // مقیاس اسکلت NPC کاهش یافت تا از کارت بیرون نزند
                    float scale = 0.18f;
                    Matrix4 transform = new Matrix4(oldTransform);
                    transform.translate(drawX, drawY, 0);
                    transform.scale(scale, scale, 1f);
                    transform.translate(-drawX, -drawY, 0);
                    batch.setTransformMatrix(transform);

                    try {
                        pamPlayer.draw(batch, pamPath, clipName, stateTime, drawX, drawY, true);
                    } catch (Exception ignored) {
                    }

                    batch.setTransformMatrix(oldTransform);
                }
            };
        }

        if (entry.iconPlant != null) {
            return new Table() {
                @Override
                public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                    super.draw(batch, parentAlpha);
                    if (textureBank != null) textureBank.update();

                    String rawName = entry.iconPlant.getName().toUpperCase().replace(" ", "").replace("-", "");
                    String pamPath = "PLANT/" + rawName + "/" + rawName + ".PAM";
                    float drawX = getX() + getWidth() / 2f;
                    float drawY = getY() + 10f;
                    try {
                        pamPlayer.draw(batch, pamPath, "idle", stateTime, drawX, drawY, true);
                    } catch (Exception ignored) {
                    }
                }
            };
        }

        return new Image(getFallbackIconBackground(entry.itemType));
    }

    private void triggerBornAnimation(int entryId) {
        currencyAnimState.put(entryId, "born");
        currencyAnimStartTime.put(entryId, stateTime);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                currencyAnimState.put(entryId, "idle");
                currencyAnimStartTime.put(entryId, stateTime);
            }
        }, 0.8f);
    }

    private Drawable getFallbackIconBackground(ItemType itemType) {
        Color color = (itemType != null && itemType.getCurrency() == Currency.GEM)
            ? Color.valueOf("4FC3D9") : Color.valueOf("E0B84F");
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture t = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(t));
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
        panel.add(titleLbl).padBottom(15).row();

        Label question = createSafeLabel("Would you like to purchase " + itemDescription + "?", "default");
        question.setWrap(true);
        question.setAlignment(Align.center);
        panel.add(question).width(320).padBottom(20).row();

        Table buttonRow = new Table();

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

        buttonRow.add(yesBtn).width(120).height(45).padRight(15);
        buttonRow.add(noBtn).width(120).height(45);
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
        for (Plant plant : unlockedPlants) {
            TextButton plantBtn = new TextButton(plant.getName(), skin, "purple");
            list.add(plantBtn).width(220).height(40).pad(4).row();
        }

        ScrollPane scrollPane = new ScrollPane(list, skin);
        scrollPane.setScrollingDisabled(true, false);
        panel.add(scrollPane).width(240).height(220).padBottom(15).row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        panel.add(cancelBtn).width(150).height(40);

        Table overlayRoot = buildOverlayRoot(panel);

        for (Actor a : list.getChildren()) {
            if (a instanceof TextButton) {
                TextButton btn = (TextButton) a;
                String plantName = btn.getText().toString();
                btn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        overlayRoot.remove();
                        showConfirmationOverlay(
                            entry.title + " (" + plantName + ")",
                            () -> completePurchase(entry, plantName)
                        );
                    }
                });
            }
        }

        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                overlayRoot.remove();
            }
        });

        stage.addActor(overlayRoot);
    }

    private void completePurchase(ShopEntry entry, String plantType) {
        String result = entry.id == 0
            ? shopMenu.buyDailyOffer(1)
            : shopMenu.buyItem(entry.id, 1, plantType);

        if (result != null && result.startsWith("Successfully")) {
            Toast.showSuccess(stage, skin, result);
            if (entry.isCurrencyConversion()) {
                triggerBornAnimation(entry.id);
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
            Pixmap pixmap = new Pixmap(1, 2, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(0.6f, 0.4f, 0.2f, 0.8f));
            pixmap.fill();
            Texture lineTexture = new Texture(pixmap);
            pixmap.dispose();
            divider.setBackground(new TextureRegionDrawable(new TextureRegion(lineTexture)));
        }
        return divider;
    }

    private Label createSafeLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        }
        return new Label(text, skin);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
