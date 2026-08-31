package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.workshop.controller.repository.Textures;
import com.workshop.model.season.AncientEgypt;
import com.workshop.model.zombie.Zombie;

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

    private final Map<String, String> ashPamPaths =
        new HashMap<>();

    private final Map<String, ZombieAnimationSpec> resolvedSpecs =
        new HashMap<>();

    private static ZombieAnimationResolver shared;

    public static ZombieAnimationResolver shared() {
        if (shared == null) {
            shared = new ZombieAnimationResolver();
        }
        return shared;
    }

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

    public ZombieAnimationSpec resolve(
        Zombie zombie,
        String seasonName
    ) {
        if (zombie == null) {
            return null;
        }

        String pamName =
            resolvePamName(zombie, seasonName);

        return resolve(pamName);
    }

    public ZombieAnimationSpec resolve(String pamName) {
        String key = normalize(pamName);

        if (resolvedSpecs.containsKey(key)) {
            return resolvedSpecs.get(key);
        }

        String pamPath = findPamPath(pamName);

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

        String walkClip = findClip(clips, "walk");

        if (walkClip == null) {
            walkClip = findClip(clips, "move");
        }

        if (walkClip != null) {
            spec.setClip(
                ZombieAnimationState.WALK,
                walkClip
            );
        }

        String eatClip = findClip(clips, "eat");

        if (eatClip == null) {
            eatClip = findClip(clips, "attack");
        }

        if (eatClip != null) {
            spec.setClip(
                ZombieAnimationState.EAT,
                eatClip
            );
        }

        String dieClip = findClip(clips, "die");
        if (dieClip == null) {
            dieClip = findClip(clips, "death");
        }
        if (dieClip != null) {
            spec.setClip(ZombieAnimationState.DIE, dieClip);
        }

        setArmlessClip(spec, ZombieAnimationState.IDLE, clips, "idle2", "idle_2");
        setArmlessClip(spec, ZombieAnimationState.WALK, clips, "walk2", "walk_2");
        setArmlessClip(spec, ZombieAnimationState.EAT, clips, "eat2", "eat_2");
        setArmlessClip(spec, ZombieAnimationState.DIE, clips, "die2", "die_2", "death2");

        String ashClip = findClipEndingWith(clips, "ash");
        if (ashClip != null) {
            spec.setClip(ZombieAnimationState.ASH, ashClip);
        }

        spec.setAshPamPath(resolveAshPam(pamName));

        Gdx.app.log(
            "ZombieAnimationResolver",
            pamName
                + " -> "
                + pamPath
                + " | clips: "
                + clips
                + " | walk: "
                + walkClip
                + " | eat: "
                + eatClip
        );

        resolvedSpecs.put(key, spec);

        return spec;
    }

    private String resolvePamName(
        Zombie zombie,
        String seasonName
    ) {
        String zombieName = zombie.getName();

        if (zombieName == null) {
            return "";
        }

        if (isNewspaper(zombie)) {
            String armoredPam = resolveArmoredPam(zombie);
            return armoredPam != null ? armoredPam : "ZOMBIE_MODERN_NEWSPAPER";
        }

        if (isBasicZombie(zombieName)) {
            String armoredPam = resolveArmoredPam(zombie);
            if (armoredPam != null) {
                return armoredPam;
            }
            return getBasicZombiePam(seasonName);
        }

        if (zombie.getId() != null
            && !zombie.getId().isBlank()) {

            return zombie.getId();
        }

        return zombieName;
    }

    private String resolveArmoredPam(Zombie zombie) {
        var armor = zombie.getArmor();
        if (armor == null || armor.isDestroyed()) {
            return null;
        }
        // Cone/Bucket/Brick are drawn as a morphing overlay on the basic body.
        return switch (armor.getArmorType()) {
            case NEWSPAPER -> "ZOMBIE_MODERN_NEWSPAPER";
            default -> null;
        };
    }

    private boolean isNewspaper(Zombie zombie) {
        String name = zombie.getName();
        String id = zombie.getId();
        return "News Paper".equalsIgnoreCase(name)
            || "Newspaper".equalsIgnoreCase(name)
            || "ZombieNewspaper".equalsIgnoreCase(id);
    }

    private boolean isBasicZombie(String zombieName) {
        return "Default".equalsIgnoreCase(zombieName)
            || "cone head".equalsIgnoreCase(zombieName)
            || "bucket head".equalsIgnoreCase(zombieName)
            || "brick head".equalsIgnoreCase(zombieName)
            || "knight".equalsIgnoreCase(zombieName);
    }

    private String getBasicZombiePam(String seasonName) {
        if (seasonName == null) {
            return "ZOMBIE_EGYPT_BASIC";
        }

        if ("FrozenCave".equalsIgnoreCase(seasonName)) {
            return "ZOMBIE_ICEAGE_BASIC";
        }

        if ("Big Wave Beach".equalsIgnoreCase(seasonName)) {
            return "ZOMBIE_BEACH_BASIC";
        }

        if ("Dark Ages".equalsIgnoreCase(seasonName)) {
            return "ZOMBIE_DARK_BASIC";
        }

        return "ZOMBIE_EGYPT_BASIC";
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

            String normalizedName = normalize(fileName);
            pamPaths.putIfAbsent(normalizedName, childPath);

            if (normalizedName.endsWith("ASH")
                && !normalizedName.endsWith("SPLASH")
                && !normalizedName.equals("SQUASH")) {
                ashPamPaths.putIfAbsent(normalizedName, childPath);
            }
        }
    }

    public String resolveAshPam(Zombie zombie) {
        if (zombie == null) {
            return fallbackAshPam();
        }
        return resolveAshPam(resolvePamName(zombie, null));
    }

    private String resolveAshPam(String pamName) {
        String key = normalize(pamName);
        String bestPath = fallbackAshPam();
        int bestScore = 0;

        String generic = ashPamPaths.get("ZOMBIEASH");
        if (generic != null) {
            bestPath = generic;
        }

        for (Map.Entry<String, String> entry : ashPamPaths.entrySet()) {
            String ashKey = entry.getKey();
            String core = ashKey;
            if (core.startsWith("ZOMBIE")) {
                core = core.substring("ZOMBIE".length());
            }
            if (core.endsWith("ASH")) {
                core = core.substring(0, core.length() - 3);
            }
            if (core.isEmpty() || core.length() < 3) {
                continue;
            }
            if ((key.contains(core) || core.contains(key)) && core.length() > bestScore) {
                bestScore = core.length();
                bestPath = entry.getValue();
            }
        }

        return bestPath;
    }

    private String fallbackAshPam() {
        String generic = ashPamPaths.get("ZOMBIEASH");
        return generic != null
            ? generic
            : "768/INITIAL/EFFECTS/ZOMBIE_ASH/ZOMBIE_ASH.PAM";
    }

    private String findPamPath(String pamName) {
        String key = normalize(pamName);

        String cleanToken = key.replace("ZOMBIE", "").replace("EGYPT", "").replace("PIRATE", "");
        if (cleanToken.equals("RA") || key.equals("ZOMBIERA") || key.equals("RAZOMBIE")) {
            return "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM";
        } if (cleanToken.contains("BARRELROLLER")){
            return "768/FULL/ZOMBIE/ZOMBIE_PIRATE_BARREL_PUSHER/ZOMBIE_PIRATE_BARREL_PUSHER.PAM";
        } if (cleanToken.contains("ZOMBOSS")){
            return "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_ZOMBOSS/ZOMBIE_EGYPT_ZOMBOSS.PAM";
        }

        String exact = pamPaths.get(key);

        if (exact != null) {
            return exact;
        }

        String coreKey = key;

        if (coreKey.startsWith("ZOMBIE")) {
            coreKey = coreKey.substring("ZOMBIE".length());
        }

        for (Map.Entry<String, String> entry : pamPaths.entrySet()) {

            String path = entry.getValue()
                .replace('\\', '/')
                .toUpperCase();

            if (!path.contains("/ZOMBIE/")) {
                continue;
            }

            String candidateKey = entry.getKey();

            if (!coreKey.isEmpty()
                && candidateKey.contains(coreKey)) {

                Gdx.app.log(
                    "ZombieAnimationResolver",
                    "Matched by ID: "
                        + pamName
                        + " -> "
                        + entry.getValue()
                );

                return entry.getValue();
            }
        }

        String upperName =
            pamName.toUpperCase();

        String[] requestedParts =
            upperName.split("[^A-Z0-9]+");

        String wantedToken =
            requestedParts[requestedParts.length - 1];

        String exactTokenMatch = findByToken(wantedToken, true);

        if (exactTokenMatch != null) {
            return exactTokenMatch;
        }

        return findByToken(wantedToken, false);
    }

    private String findByToken(String wantedToken, boolean exact) {

        for (Map.Entry<String, String> entry
            : pamPaths.entrySet()) {

            String path =
                entry.getValue()
                    .replace('\\', '/')
                    .toUpperCase();

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

                boolean matches = exact
                    ? token.equals(wantedToken)
                    : (wantedToken.length() >= 4
                    && token.length() >= 4
                    && (token.startsWith(wantedToken)
                    || wantedToken.startsWith(token)));

                if (matches) {

                    Gdx.app.log(
                        "ZombieAnimationResolver",
                        (exact ? "Matched " : "Loosely matched ")
                            + wantedToken
                            + " -> "
                            + entry.getValue()
                    );

                    return entry.getValue();
                }
            }
        }

        return null;
    }

    private void setArmlessClip(
        ZombieAnimationSpec spec,
        ZombieAnimationState state,
        List<String> clips,
        String... names
    ) {
        for (String name : names) {
            String found = findExactClip(clips, name);
            if (found == null) {
                found = findClipEndingWith(clips, name);
            }
            if (found != null) {
                spec.setArmlessClip(state, found);
                return;
            }
        }
    }

    private String findExactClip(List<String> clips, String expectedName) {
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

    private String findClipEndingWith(
        List<String> clips,
        String suffix
    ) {
        if (clips == null) {
            return null;
        }

        String normalizedSuffix = normalize(suffix);
        String found = null;
        for (String clip : clips) {
            if (normalize(clip).endsWith(normalizedSuffix)) {
                found = clip;
            }
        }
        return found;
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
