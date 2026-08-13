package pvz.browser.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class BrowserContext {
    public static TextureBank textures;
    public static PamPlayer player;
    public static pvz.browser.core.BrowserAppSettings settings;

    public static void init() {
        FileHandle rootDir = Gdx.files.internal("assets");
        if (!rootDir.exists()) {
            rootDir = Gdx.files.internal("");
        }

        FileHandle imagesDir = rootDir.child("IMAGES");
        FileHandle atlasesDir = rootDir.child("ATLASES");
        FileHandle resourcesFile = rootDir.child("RESOURCES.json");
        FileHandle exportsDir = rootDir.child("Exports");

        settings = new pvz.browser.core.BrowserAppSettings(
            imagesDir.path(),
            resourcesFile.path(),
            atlasesDir.path(),
            exportsDir.path()
        );

        textures = new TextureBank("768", rootDir);
        player = new PamPlayer(textures, rootDir);
    }
}
