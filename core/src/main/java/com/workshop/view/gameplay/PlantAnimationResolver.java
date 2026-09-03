package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.workshop.controller.repository.Textures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlantAnimationResolver {

    private static final String PLANT_ROOT =
        "IMAGES/768";

    private static final String PAM_PREFIX =
        "768";

    private final Map<String, String> pamPaths =
        new HashMap<>();

    private final Map<String, PlantAnimationSpec> resolvedSpecs =
        new HashMap<>();

    private static PlantAnimationResolver shared;

    public static PlantAnimationResolver shared() {
        if (shared == null) {
            shared = new PlantAnimationResolver();
        }
        return shared;
    }

    public PlantAnimationResolver() {
        FileHandle root =
            Textures.assetsRoot().child(PLANT_ROOT);

        if (root.exists()) {
            scanDirectory(root, PAM_PREFIX);
        } else {
            Gdx.app.error(
                "PlantAnimationResolver",
                "Plant animation folder not found: " + root.path()
            );
        }

        FileHandle loosePlants =
            Textures.assetsRoot().child("IMAGES/PLANT");
        if (loosePlants.exists() && loosePlants.isDirectory()) {
            scanDirectory(loosePlants, "PLANT");
        }

        List<String> sunClips =
            Textures.getPamPlayer().clips(
                "768/INITIAL/EFFECTS/SUN/SUN.PAM"
            );

        Gdx.app.log(
            "SUN TEST",
            "SUN clips: " + sunClips
        );

        List<String> radioactiveClips =
            Textures.getPamPlayer().clips(
                "768/FULL/EFFECTS/SUN_BOMB/SUN_BOMB.PAM"
            );

        Gdx.app.log(
            "SUN TEST",
            "SUN_BOMB clips: " + radioactiveClips
        );
    }

    public PlantAnimationSpec resolve(String plantName) {
        String key = normalize(plantName);

        if (resolvedSpecs.containsKey(key)) {
            return resolvedSpecs.get(key);
        }

        String pamPath = pamPaths.get(key);
        if (pamPath == null) {
            String alias = plantPamAlias(key);
            if (alias != null) {
                pamPath = pamPaths.get(alias);
            }
        }

        if (pamPath == null) {
            Gdx.app.error(
                "PlantAnimationResolver",
                "No PAM found for plant: " + plantName
            );
            resolvedSpecs.put(key, null);
            return null;
        }

        // Do not call clips() here: it loadSyncs atlases on the render thread
        // and freezes the game the first time a plant like Peashooter is chosen.
        PlantAnimationSpec spec =
            new PlantAnimationSpec(pamPath, "idle");
        spec.setClip(PlantAnimationState.ATTACK, "attack");
        spec.setClip(PlantAnimationState.SPECIAL, "special");
        spec.setClip(PlantAnimationState.PLANTFOOD, "plantfood");

        Gdx.app.log(
            "PlantAnimationResolver",
            plantName + " -> " + pamPath
        );

        resolvedSpecs.put(key, spec);
        return spec;
    }

    private void scanDirectory(
        FileHandle directory,
        String relativePath
    ) {
        for (FileHandle child : directory.list()) {
            String childPath =
                relativePath + "/" + child.name();

            if (child.isDirectory()) {
                scanDirectory(child, childPath);
                continue;
            }

            if (!"pam".equalsIgnoreCase(
                child.extension()
            )) {
                continue;
            }

            String normalizedPath =
                childPath
                    .replace('\\', '/')
                    .toUpperCase();

            if (!normalizedPath.contains("/PLANT/")
                && !normalizedPath.contains("/EMPOWERMINTS/")
                && !normalizedPath.startsWith("PLANT/")) {
                continue;
            }

            String fileName =
                child.nameWithoutExtension();

            pamPaths.putIfAbsent(
                normalize(fileName),
                stripImagesPrefix(childPath)
            );
        }
    }

    private static String stripImagesPrefix(String path) {
        if (path == null) {
            return null;
        }

        String normalized = path.replace('\\', '/');
        while (normalized.regionMatches(true, 0, "IMAGES/", 0, 7)) {
            normalized = normalized.substring(7);
        }
        return normalized;
    }

    private String normalize(String name) {
        if (name == null) {
            return "";
        }

        return name
            .replaceAll("[^A-Za-z0-9]", "")
            .toUpperCase();
    }

    private static String plantPamAlias(String normalizedName) {
        if ("PHATBEET".equals(normalizedName)) {
            return "PHATBEETS";
        }
        if ("WASABI".equals(normalizedName)) {
            return "WASABIWHIP";
        }
        if ("ROTOBAGA".equals(normalizedName)) {
            return "ROTORUTABAGA";
        }
        return null;
    }
}
