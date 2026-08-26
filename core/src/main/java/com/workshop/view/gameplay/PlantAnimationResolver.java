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

    public PlantAnimationResolver() {
        FileHandle root =
            Textures.assetsRoot().child(PLANT_ROOT);

        if (!root.exists()) {
            Gdx.app.error(
                "PlantAnimationResolver",
                "Plant animation folder not found: " + root.path()
            );
            return;
        }

        scanDirectory(root, PAM_PREFIX);

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
            Gdx.app.error(
                "PlantAnimationResolver",
                "No PAM found for plant: " + plantName
            );
            return null;
        }

        List<String> clips =
            Textures.getPamPlayer().clips(pamPath);

        logAvailableClips(
            plantName,
            pamPath,
            clips
        );

        String idleClip = findClip(clips, "idle");

        if (idleClip == null) {
            Gdx.app.error(
                "PlantAnimationResolver",
                "No idle clip found for: " + plantName
            );
            return null;
        }

        PlantAnimationSpec spec =
            new PlantAnimationSpec(pamPath, idleClip);

        String attackClip = findClip(clips, "attack");

        if (attackClip != null) {
            spec.setClip(
                PlantAnimationState.ATTACK,
                attackClip
            );
        }

        String specialClip = findClip(clips, "special");

        if (specialClip != null) {
            spec.setClip(
                PlantAnimationState.SPECIAL,
                specialClip
            );
        }

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

            if (!normalizedPath.contains("/PLANT/")) {
                continue;
            }

            String fileName =
                child.nameWithoutExtension();

            pamPaths.putIfAbsent(
                normalize(fileName),
                childPath
            );
        }
    }

    private String findClip(
        List<String> clips,
        String expectedName
    ) {
        if (clips == null) {
            return null;
        }

        for (String clip : clips) {
            if (expectedName.equalsIgnoreCase(clip)) {
                return clip;
            }
        }

        return null;
    }

    private String normalize(String name) {
        if (name == null) {
            return "";
        }

        return name
            .replaceAll("[^A-Za-z0-9]", "")
            .toUpperCase();
    }

    private void logAvailableClips(
        String plantName,
        String pamPath,
        List<String> clips
    ) {
        Gdx.app.log(
            "PlantAnimationResolver",
            plantName + " -> " + pamPath
                + " | clips: " + clips
        );
    }
}
