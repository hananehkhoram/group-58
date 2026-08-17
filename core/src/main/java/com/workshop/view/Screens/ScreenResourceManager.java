package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.workshop.controller.repository.Textures;
import pvz.libpvz.pam.PamPlayer;

/**
 * Centralized resource manager for resolving animation and texture paths.
 * Handles PAM file path normalization and multiple fallback strategies.
 * Reduces code duplication across screens by consolidating path-resolution logic.
 */
public final class ScreenResourceManager {

    private ScreenResourceManager() {}

    /**
     * Resolves a plant name to the folder name used in PAM assets.
     * Handles special cases where the plant name format doesn't match the PAM folder name.
     *
     * Example transformations:
     *   "Peashooter" → "PEASHOOTER"
     *   "Snow Pea" → "SNOWPEA"
     *   "Primal Potato Mine" → "PRIMAL_POTATOMINE"
     *
     * @param plantName The display name of the plant (e.g., "Peashooter")
     * @return The normalized folder name (e.g., "PEASHOOTER")
     */
    public static String resolvePlantFolderName(String plantName) {
        String raw = plantName.toUpperCase();
        String normalized = raw.replace(" ", "").replace("-", "");

        // Handle special cases where normalized name doesn't match PAM folder
        if (normalized.equalsIgnoreCase("PRIMALPOTATOMINE")) {
            return "PRIMAL_POTATOMINE";
        }

        return normalized;
    }

    /**
     * Resolves a zombie name to the folder name used in PAM assets.
     * Handles underscores and multi-word zombie names.
     *
     * Example transformations:
     *   "Basic Zombie" → tries "BASICZOMBIE" then "BASIC_ZOMBIE"
     *   "Imp" → "IMP"
     *
     * @param zombieName The display name of the zombie
     * @return The normalized folder name
     */
    public static String resolveZombieFolderName(String zombieName) {
        String raw = zombieName.toUpperCase();
        // Try compact version first (no spaces)
        return raw.replace(" ", "");
    }

    /**
     * Attempts to draw a plant PAM animation with multiple path fallback strategies.
     * Tries several common path patterns before falling back to static Texture lookup.
     *
     * @param batch     The render batch
     * @param pamPlayer The PAM animation player
     * @param plantName The plant name (e.g., "Peashooter")
     * @param clip      The animation clip name (e.g., "idle", "idle_stage1", "intro", etc.)
     * @param stateTime Current animation time (for looping)
     * @param drawX     X position to draw at
     * @param drawY     Y position to draw at
     * @param isLocked  If true, draws at reduced brightness
     * @return true if animation was successfully drawn, false if fallback was used
     */
    public static boolean drawPlantAnimation(
        Batch batch,
        PamPlayer pamPlayer,
        String plantName,
        String clip,
        float stateTime,
        float drawX,
        float drawY,
        boolean isLocked
    ) {
        if (pamPlayer == null) {
            return drawPlantFallback(batch, plantName, drawX, drawY);
        }

        if (isLocked) {
            batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        }

        String folderName = resolvePlantFolderName(plantName);

        // Paths to try in order
        String[] possiblePaths = {
            "PLANT/" + folderName + "/" + folderName + ".PAM",
            "768/INITIAL/PLANT/" + folderName + "/" + folderName + ".PAM",
            "IMAGES/PLANT/" + folderName + "/" + folderName + ".PAM"
        };

        // Skip known problematic plants
        if (folderName.equalsIgnoreCase("CATTAILMINT") ||
            folderName.equalsIgnoreCase("CATTAIL")) {
            batch.setColor(Color.WHITE);
            return drawPlantFallback(batch, plantName, drawX, drawY);
        }

        // لیست کلیپ‌ها جهت تست: ابتدا کلیپ درخواستی، سپس کلیپ‌های عمومی جهت fallback
        String activeClip = (clip != null && !clip.trim().isEmpty()) ? clip : "idle";
        String[] clipsToTry = activeClip.equalsIgnoreCase("idle")
            ? new String[]{"idle"}
            : new String[]{activeClip, "idle"};

        for (String pamPath : possiblePaths) {
            for (String currentClip : clipsToTry) {
                try {
                    pamPlayer.draw(batch, pamPath, currentClip, stateTime, drawX, drawY, true);
                    batch.setColor(Color.WHITE);
                    return true;
                } catch (Exception e) {
                    Gdx.app.debug("ScreenResourceManager",
                        "Failed clip '" + currentClip + "' for path: " + pamPath + " (" + e.getMessage() + ")");
                }
            }
        }

        batch.setColor(Color.WHITE);
        return drawPlantFallback(batch, plantName, drawX, drawY);
    }

    /**
     * Draws a static texture fallback when PAM animation is unavailable.
     *
     * @param batch The render batch
     * @param plantName The plant name
     * @param drawX X position
     * @param drawY Y position
     * @return true if a texture was found and drawn, false otherwise
     */
    private static boolean drawPlantFallback(Batch batch, String plantName, float drawX, float drawY) {
        String rawName = plantName.toUpperCase().replace(" ", "_");
        TextureRegion reg = Textures.regionOrNull("PLANT_" + rawName);
        if (reg != null) {
            batch.draw(reg, drawX - 30, drawY, 60, 60);
            return true;
        }
        return false;
    }

    /**
     * Attempts to draw a zombie PAM animation with multiple path fallback strategies.
     * Similar to drawPlantAnimation, but for zombies.
     *
     * @param batch The render batch
     * @param pamPlayer The PAM animation player
     * @param zombieName The zombie name
     * @param stateTime Current animation time
     * @param drawX X position to draw at
     * @param drawY Y position to draw at
     * @return true if animation was successfully drawn
     */
    public static boolean drawZombieAnimation(
        Batch batch,
        PamPlayer pamPlayer,
        String zombieName,
        float stateTime,
        float drawX,
        float drawY
    ) {
        if (pamPlayer == null) {
            return drawZombieFallback(batch, zombieName, drawX, drawY);
        }

        String folderName = resolveZombieFolderName(zombieName);

        // Paths to try in order
        String[] possiblePaths = {
            "768/INITIAL/ZOMBIE/" + folderName + "/" + folderName + ".PAM",
            "ZOMBIE/" + folderName + "/" + folderName + ".PAM",
            "768/INITIAL/ZOMBIES/" + folderName + "/" + folderName + ".PAM"
        };

        for (String pamPath : possiblePaths) {
            try {
                if (pamPlayer != null) {
                    // Try multiple animation clips
                    String[] clips = {"idle", "anim_idle", "walk", "anim_walk"};
                    for (String clip : clips) {
                        try {
                            pamPlayer.draw(batch, pamPath, clip, stateTime, drawX, drawY, true);
                            return true;
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception e) {
                Gdx.app.debug("ScreenResourceManager",
                    "Failed to load zombie PAM: " + pamPath);
            }
        }

        return drawZombieFallback(batch, zombieName, drawX, drawY);
    }

    /**
     * Draws a static texture fallback when zombie PAM animation is unavailable.
     *
     * @param batch The render batch
     * @param zombieName The zombie name
     * @param drawX X position
     * @param drawY Y position
     * @return true if a texture was found and drawn
     */
    private static boolean drawZombieFallback(Batch batch, String zombieName, float drawX, float drawY) {
        String rawName = zombieName.toUpperCase().replace(" ", "_");
        TextureRegion reg = Textures.regionOrNull("ZOMBIE_" + rawName);
        if (reg != null) {
            batch.draw(reg, drawX - 25, drawY, 55, 55);
            return true;
        }
        return false;
    }
}
