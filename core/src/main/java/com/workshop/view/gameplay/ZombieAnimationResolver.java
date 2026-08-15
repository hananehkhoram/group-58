package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.workshop.controller.repository.Textures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ZombieAnimationResolver {

    private static final String ZOMBIE_ROOT =
        "IMAGES/768";

    private static final String PAM_PREFIX =
        "768";

    private final Map<String, String> pamPaths =
        new HashMap<>();

    private final Map<String, ZombieAnimationSpec> resolvedSpecs =
        new HashMap<>();

    public ZombieAnimationResolver() {
        FileHandle root =
            Textures.assetsRoot().child(ZOMBIE_ROOT);

        if (!root.exists()) {
            Gdx.app.error(
                "ZombieAnimationResolver",
                "Zombie animation folder not found: "
                    + root.path()
            );
            return;
        }

        scanDirectory(root, PAM_PREFIX);
    }

    public ZombieAnimationSpec resolve(String pamName) {
        String key = normalize(pamName);

        if (resolvedSpecs.containsKey(key)) {
            return resolvedSpecs.get(key);
        }

        String pamPath = findPamPath(key);

        if (pamPath == null) {
            Gdx.app.error(
                "ZombieAnimationResolver",
                "No PAM found for zombie: " + pamName
            );
            return null;
        }

        List<String> clips =
            Textures.getPamPlayer().clips(pamPath);

        String idleClip = findClip(clips, "idle");

        if (idleClip == null) {
            idleClip = findClip(clips, "animation");
        }

        if (idleClip == null && clips != null && clips.size() == 1) {
            idleClip = clips.get(0);
        }

        if (idleClip == null) {
            Gdx.app.error(
                "ZombieAnimationResolver",
                "No usable idle clip found for: "
                    + pamName
                    + " | clips: "
                    + clips
            );
            return null;
        }

        ZombieAnimationSpec spec =
            new ZombieAnimationSpec(
                pamPath,
                idleClip
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
                scanDirectory(
                    child,
                    childPath
                );
                continue;
            }

            if (!"pam".equalsIgnoreCase(
                child.extension()
            )) {
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

    private String findPamPath(String pamName) {

        String key = normalize(pamName);

        // اول exact match
        String exact = pamPaths.get(key);

        if (exact != null) {
            return exact;
        }

        String upperName =
            pamName.toUpperCase();

        String[] requestedParts =
            upperName.split("[^A-Z0-9]+");

        String wantedToken =
            requestedParts[requestedParts.length - 1];

        for (Map.Entry<String, String> entry
            : pamPaths.entrySet()) {

            String path =
                entry.getValue()
                    .replace('\\', '/')
                    .toUpperCase();

            // فقط PAMهای واقعی بخش Zombie
            if (!path.contains("/ZOMBIE/")) {
                continue;
            }

            int lastSlash =
                path.lastIndexOf('/');

            String fileName =
                lastSlash >= 0
                    ? path.substring(lastSlash + 1)
                    : path;

            if (fileName.endsWith(".PAM")) {
                fileName =
                    fileName.substring(
                        0,
                        fileName.length() - 4
                    );
            }

            String[] tokens =
                fileName.split("[^A-Z0-9]+");

            for (String token : tokens) {

                if (token.equals(wantedToken)) {

                    Gdx.app.log(
                        "ZombieAnimationResolver",
                        "Matched "
                            + pamName
                            + " -> "
                            + entry.getValue()
                    );

                    return entry.getValue();
                }
            }
        }

        return null;
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

        String normalizedExpected =
            normalize(expectedName);

        for (String clip : clips) {
            String normalizedClip =
                normalize(clip);

            if (normalizedClip.contains(
                normalizedExpected
            )) {
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
}
