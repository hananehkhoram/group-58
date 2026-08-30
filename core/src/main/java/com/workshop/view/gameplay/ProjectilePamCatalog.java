package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.workshop.controller.repository.Textures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pvz.libpvz.pam.PamPlayer;

final class ProjectilePamCatalog {

    private static final String LOG_TAG = "ProjectilePamCatalog";

    private final PamPlayer pamPlayer;
    private final List<Candidate> candidates = new ArrayList<>();
    private final Map<String, Boolean> entityAnimationCache = new HashMap<>();
    private final Set<String> inspectionErrors = new HashSet<>();

    ProjectilePamCatalog(PamPlayer pamPlayer) {
        this.pamPlayer = pamPlayer;
        scanAllPamFiles();
    }

    List<Candidate> getCandidates() {
        return Collections.unmodifiableList(candidates);
    }

    boolean isPlantEntityAnimation(Candidate candidate) {
        if (isProjectileAssetPath(candidate.getPath())) {
            return false;
        }

        Boolean cached = entityAnimationCache.get(candidate.getPath());
        if (cached != null) {
            return cached;
        }

        boolean entityAnimation = inspectPlantEntityAnimation(candidate);
        entityAnimationCache.put(candidate.getPath(), entityAnimation);
        return entityAnimation;
    }

    static boolean isProjectileAssetPath(String path) {
        if (path == null) {
            return false;
        }
        String upper = path.replace('\\', '/').toUpperCase(Locale.ROOT);
        return upper.contains("PROJECTILE")
            || upper.contains("/PEA/")
            || upper.endsWith("/PEA.PAM")
            || upper.contains("BULLET");
    }

    private void scanAllPamFiles() {
        FileHandle imagesRoot = Textures.assetsRoot().child("IMAGES");

        if (!imagesRoot.exists() || !imagesRoot.isDirectory()) {
            Gdx.app.error(
                LOG_TAG,
                "IMAGES folder not found: " + imagesRoot.file().getAbsolutePath()
            );
            return;
        }

        collectPamFiles(imagesRoot, "");
        candidates.sort(Comparator.comparing(
            Candidate::getPath,
            String.CASE_INSENSITIVE_ORDER
        ));

        Gdx.app.log(
            LOG_TAG,
            "Indexed " + candidates.size() + " PAM files under "
                + imagesRoot.file().getAbsolutePath()
        );
    }

    private void collectPamFiles(FileHandle directory, String relativePath) {
        for (FileHandle child : directory.list()) {
            String childPath = relativePath.isEmpty()
                ? child.name()
                : relativePath + "/" + child.name();

            if (child.isDirectory()) {
                collectPamFiles(child, childPath);
                continue;
            }

            if (!"pam".equalsIgnoreCase(child.extension())) {
                continue;
            }

            candidates.add(new Candidate(childPath));
        }
    }

    private boolean inspectPlantEntityAnimation(Candidate candidate) {
        try {
            List<String> clips = pamPlayer.clips(candidate.getPath());
            boolean hasIdle = false;
            boolean hasAttack = false;
            boolean hasPlantFood = false;

            for (String clip : clips) {
                if (clip == null) {
                    continue;
                }

                String normalized = clip.trim().toLowerCase(Locale.ROOT);
                hasIdle |= "idle".equals(normalized);
                hasAttack |= "attack".equals(normalized);
                hasPlantFood |= normalized.startsWith("plantfood");
            }

            return hasIdle && (hasAttack || hasPlantFood);
        } catch (RuntimeException exception) {
            logInspectionError(candidate, exception);
            return false;
        }
    }

    private void logInspectionError(Candidate candidate, RuntimeException exception) {
        if (!inspectionErrors.add(candidate.getPath())) {
            return;
        }

        Gdx.app.error(
            LOG_TAG,
            "Could not inspect PAM clips: " + candidate.getPath(),
            exception
        );
    }

    static Set<String> tokenize(String value) {
        Set<String> result = new HashSet<>();
        if (value == null || value.isBlank()) {
            return result;
        }

        String camelSeparated = value.replaceAll(
            "([a-z0-9])([A-Z])",
            "$1 $2"
        );

        String[] parts = camelSeparated
            .toUpperCase(Locale.ROOT)
            .split("[^A-Z0-9]+");

        for (String part : parts) {
            if (part.length() <= 1 || isNumeric(part)) {
                continue;
            }
            result.add(part);
        }

        return result;
    }

    static String compact(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replaceAll("[^A-Za-z0-9]", "")
            .toUpperCase(Locale.ROOT);
    }

    private static boolean isNumeric(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }

    static final class Candidate {

        private final String path;
        private final Set<String> fullTokens;
        private final Set<String> localTokens;
        private final String compactPath;

        Candidate(String path) {
            this.path = path.replace('\\', '/');
            this.fullTokens = Collections.unmodifiableSet(tokenize(this.path));
            this.localTokens = Collections.unmodifiableSet(tokenize(buildLocalIdentity(this.path)));
            this.compactPath = compact(stripExtension(this.path));
        }

        String getPath() {
            return path;
        }

        Set<String> getFullTokens() {
            return fullTokens;
        }

        Set<String> getLocalTokens() {
            return localTokens;
        }

        String getCompactPath() {
            return compactPath;
        }

        private static String buildLocalIdentity(String path) {
            String normalized = path.replace('\\', '/');
            String withoutExtension = stripExtension(normalized);
            int lastSlash = withoutExtension.lastIndexOf('/');

            if (lastSlash < 0) {
                return withoutExtension;
            }

            String fileName = withoutExtension.substring(lastSlash + 1);
            String parentPath = withoutExtension.substring(0, lastSlash);
            int parentSlash = parentPath.lastIndexOf('/');
            String parentName = parentSlash < 0
                ? parentPath
                : parentPath.substring(parentSlash + 1);

            return parentName + " " + fileName;
        }

        private static String stripExtension(String path) {
            int slash = path.lastIndexOf('/');
            int dot = path.lastIndexOf('.');

            if (dot > slash) {
                return path.substring(0, dot);
            }
            return path;
        }
    }
}
