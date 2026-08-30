package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.workshop.controller.repository.Textures;
import com.workshop.view.gameplay.PlantAnimationResolver;
import com.workshop.view.gameplay.PlantAnimationSpec;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized resource manager for resolving animation and texture paths.
 * Handles PAM file path normalization and multiple fallback strategies.
 */
public final class ScreenResourceManager {

    private static final Map<String, String> existingPamPaths = new HashMap<>();

    private ScreenResourceManager() {}

    public static String resolvePlantFolderName(String plantName) {
        String raw = plantName.toUpperCase();
        String normalized = raw.replace(" ", "").replace("-", "");

        if (normalized.equalsIgnoreCase("PRIMALPOTATOMINE")) {
            return "PRIMAL_POTATOMINE";
        }

        return normalized;
    }

    public static String resolveZombieFolderName(String zombieName) {
        String raw = zombieName.toUpperCase().trim();
        String normalized = raw.replace("ZOMBIE", "").replace(" ", "").replace("-", "");
        return normalized.isEmpty() ? raw : normalized;
    }

    private static String existingPamPath(String plantName, String folderName) {
        if (existingPamPaths.containsKey(plantName)) {
            return existingPamPaths.get(plantName);
        }

        PlantAnimationSpec spec = PlantAnimationResolver.shared().resolve(plantName);
        if (spec != null && pamFileExists(spec.getPamPath())) {
            existingPamPaths.put(plantName, spec.getPamPath());
            return spec.getPamPath();
        }

        String[] candidates = {
            "768/INITIAL/EMPOWERMINTS/PLANT/" + folderName + "/" + folderName + ".PAM",
            "768/FULL/EMPOWERMINTS/PLANT/" + folderName + "/" + folderName + ".PAM",
            "768/INITIAL/PLANT/" + folderName + "/" + folderName + ".PAM",
            "768/FULL/PLANT/" + folderName + "/" + folderName + ".PAM",
            "PLANT/" + folderName + "/" + folderName + ".PAM"
        };

        for (String candidate : candidates) {
            if (pamFileExists(candidate)) {
                existingPamPaths.put(plantName, candidate);
                return candidate;
            }
        }

        existingPamPaths.put(plantName, null);
        return null;
    }

    private static boolean pamFileExists(String relativeToImages) {
        if (relativeToImages == null || relativeToImages.isBlank()) {
            return false;
        }

        FileHandle file = Textures.assetsRoot()
            .child("IMAGES")
            .child(relativeToImages.replace('\\', '/'));
        return file.exists() && !file.isDirectory();
    }

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

        if (folderName.equalsIgnoreCase("CATTAILMINT") ||
            folderName.equalsIgnoreCase("CATTAIL")) {
            batch.setColor(Color.WHITE);
            return drawPlantFallback(batch, plantName, drawX, drawY);
        }

        String pamPath = existingPamPath(plantName, folderName);
        if (pamPath == null) {
            batch.setColor(Color.WHITE);
            return drawPlantFallback(batch, plantName, drawX, drawY);
        }

        String activeClip = (clip != null && !clip.trim().isEmpty()) ? clip : "idle";
        String[] clipsToTry = activeClip.equalsIgnoreCase("idle")
            ? new String[]{"idle", "idle_stage1", "loop"}
            : new String[]{activeClip, "idle", "idle_stage1", "loop"};

        for (String currentClip : clipsToTry) {
            try {
                pamPlayer.draw(batch, pamPath, currentClip, stateTime, drawX, drawY, true);
                batch.setColor(Color.WHITE);
                return true;
            } catch (Throwable e) {
                Gdx.app.debug("ScreenResourceManager",
                    "Failed clip '" + currentClip + "' for path: " + pamPath + " (" + e.getMessage() + ")");
            }
        }

        batch.setColor(Color.WHITE);
        return drawPlantFallback(batch, plantName, drawX, drawY);
    }

    private static boolean drawPlantFallback(Batch batch, String plantName, float drawX, float drawY) {
        String rawName = plantName.toUpperCase().replace(" ", "_");
        TextureRegion reg = Textures.regionOrNull("PLANT_" + rawName);
        if (reg != null) {
            batch.draw(reg, drawX - 30, drawY, 60, 60);
            return true;
        }
        return false;
    }

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

        String[] possiblePaths = {
            "ZOMBIE/" + folderName + "/" + folderName + ".PAM",
            "../../ZOMBIE/" + folderName + "/" + folderName + ".PAM",
            "ZOMBIE/ZOMBIE_" + folderName + "/ZOMBIE_" + folderName + ".PAM"
        };

        String[] clips = {"idle", "anim_idle", "walk", "anim_walk"};

        for (String pamPath : possiblePaths) {
            for (String clip : clips) {
                try {
                    pamPlayer.draw(batch, pamPath, clip, stateTime, drawX, drawY, true);
                    return true;
                } catch (Throwable ignored) {
                }
            }
        }

        return drawZombieFallback(batch, zombieName, drawX, drawY);
    }

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
