package com.workshop.controller.repository;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.libpvz.pam.PamPlayer;

import pvz.libpvz.textures.TextureBank;

/**
 * Holds the single {@link TextureBank} used to look up PvZ2 UI/game image regions
 * (via libPVZ) by their resource ID, e.g. "IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL".
 *
 * Requires the extracted game assets (IMAGES/, ATLASES/, RESOURCES.json) to be
 * present under the project's assets/ folder — libPVZ ships no game assets itself,
 * you must supply your own from a legally owned copy of the game.
 *
 * Doesn't rely on the working directory being set to assets/ (build.gradle's
 * workingDir only applies when actually launched through Gradle — IDE "run main()
 * directly" configurations ignore it and default to the project root instead).
 * Instead this tries a handful of likely locations relative to wherever the JVM
 * actually started, and uses whichever one actually has ATLASES/ in it.
 */
public final class Textures {

    private static TextureBank instance;
    private static FileHandle cachedAssetsRoot;
    private static PamPlayer pamPlayer;

    private Textures() {}

    public static TextureBank getInstance() {
        if (instance == null) {
            instance = new TextureBank("768", assetsRoot());
        }
        return instance;
    }

    public static PamPlayer getPamPlayer() {
        if (pamPlayer == null) {
            pamPlayer = new PamPlayer(
                getInstance(),
                assetsRoot()
            );
        }
        return pamPlayer;
    }

    /**
     * The resolved assets/ folder, found by trying a handful of likely locations
     * relative to wherever the JVM actually started (see class docs). Reuse this
     * for any other raw file under assets/ — e.g.
     * {@code Textures.assetsRoot().child("IMAGES/Menus/profile/img.png")} — instead
     * of {@code Gdx.files.internal(...)}, which depends on the classpath copy of
     * resources being up to date and ignores this same working-directory problem.
     */
    public static FileHandle assetsRoot() {
        if (cachedAssetsRoot == null) {
            cachedAssetsRoot = locateAssetsRoot();
        }
        return cachedAssetsRoot;
    }

    private static FileHandle locateAssetsRoot() {
        String[] candidates = {
            "",              // cwd IS the assets folder (e.g. Gradle's workingDir worked)
            "assets",        // cwd is the project root (typical for IDE "run main()" configs)
            "../assets",     // cwd is a module folder like lwjgl3/ or core/
            "../../assets"   // cwd is nested a module subfolder deeper (e.g. build output)
        };

        for (String candidate : candidates) {
            FileHandle root = Gdx.files.local(candidate);
            boolean found = root.child("ATLASES").exists() || root.child("atlases").exists();
            Gdx.app.log("Textures", "Checked \"" + candidate + "\" -> "
                + root.file().getAbsolutePath() + " (ATLASES found: " + found + ")");
            if (found) return root;
        }

        // Nothing found — fall back to cwd so the error message at least shows
        // a real path to compare against where ATLASES/ actually lives.
        Gdx.app.error("Textures", "Could not locate ATLASES/ under any candidate path. "
            + "Falling back to the working directory, which will likely fail below.");
        return Gdx.files.local("");
    }

    /** Convenience: looks up a region, returning null (instead of throwing) if it's missing. */
    public static TextureRegion regionOrNull(String imageResourceId) {
        try {
            return getInstance().region(imageResourceId);
        } catch (Exception e) {
            Gdx.app.error("Textures", "Failed to load region " + imageResourceId, e);
            return null;
        }
    }

}
