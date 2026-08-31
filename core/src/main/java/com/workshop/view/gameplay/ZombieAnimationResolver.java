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

    /** Only extracted basic-zombie PAM in this asset pack. */
    private static final String EGYPT_BASIC_PAM =
        "768/FULL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM";

    /** Generic Imp PAM that is actually extracted. */
    private static final String TUTORIAL_IMP_PAM =
        "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL_IMP/ZOMBIE_TUTORIAL_IMP.PAM";

    private boolean loggedMissingSeasonBasic;
    private boolean loggedMissingSeasonImp;

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
            resolvedSpecs.put(key, null);
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

        if (isImp(zombie)) {
            return getImpZombiePam(seasonName);
        }

        if (isJuggler(zombie)) {
            return "ZOMBIE_DARK_JESTER";
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

    private boolean isImp(Zombie zombie) {
        String name = zombie.getName();
        String id = zombie.getId();
        return "Imp".equalsIgnoreCase(name)
            || "ZombieImp".equalsIgnoreCase(id);
    }

    private String getImpZombiePam(String seasonName) {
        if (seasonName == null) {
            return "ZOMBIE_TUTORIAL_IMP";
        }

        if ("FrozenCave".equalsIgnoreCase(seasonName)) {
            return "ZOMBIE_ICEAGE_IMP";
        }

        if ("Big Wave Beach".equalsIgnoreCase(seasonName)) {
            return "ZOMBIE_BEACH_IMP_MERMAID";
        }

        if ("Dark Ages".equalsIgnoreCase(seasonName)) {
            return "ZOMBIE_DARK_IMP_MONK";
        }

        if ("Ancient Egypt".equalsIgnoreCase(seasonName)) {
            return "ZOMBIE_EGYPT_IMP";
        }

        return "ZOMBIE_TUTORIAL_IMP";
    }

    private boolean isJuggler(Zombie zombie) {
        String name = zombie.getName();
        String id = zombie.getId();
        return "Juggler".equalsIgnoreCase(name)
            || "ZombieDarkJuggler".equalsIgnoreCase(id);
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
        }
        if (key.contains("JUGGLER")) {
            String jesterKey = key.replace("JUGGLER", "JESTER");
            String jester = pamPaths.get(jesterKey);
            if (jester == null) {
                jester = pamPaths.get("ZOMBIEDARKJESTER");
            }
            if (jester != null) {
                return jester;
            }
        }
        if (looksLikeZomboss(key)) {
            String zombossPath = findScannedZombossPath(key);
            if (zombossPath != null) {
                return zombossPath;
            }
        }

        String exact = pamPaths.get(key);

        if (exact != null) {
            return exact;
        }

        String missingSeasonBasic = missingSeasonBasicFallback(key);
        if (missingSeasonBasic != null) {
            return missingSeasonBasic;
        }

        String missingSeasonImp = missingSeasonImpFallback(key);
        if (missingSeasonImp != null) {
            return missingSeasonImp;
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
                && coreKey.length() >= 5
                && !coreKey.equals("ZOMBIE")
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

    /**
     * Dark / Ice Age / Beach peasant PAMs are listed in RESOURCES.json but were
     * never extracted. Use the Egypt peasant until those folders exist on disk.
     */
    private String missingSeasonBasicFallback(String key) {
        boolean missingPeasant =
            key.contains("DARKBASIC")
                || key.contains("ICEAGEBASIC")
                || key.contains("BEACHBASIC");

        if (!missingPeasant) {
            return null;
        }

        String egypt = pamPaths.get("ZOMBIEEGYPTBASIC");
        if (egypt == null) {
            egypt = EGYPT_BASIC_PAM;
        }

        if (!loggedMissingSeasonBasic) {
            loggedMissingSeasonBasic = true;
            Gdx.app.log(
                "ZombieAnimationResolver",
                "Season peasant PAM is not on disk (" + key
                    + "). Using Egypt basic until zombie_dark_basic / "
                    + "zombie_iceage_basic / zombie_beach_basic are extracted."
            );
        }

        return egypt;
    }

    /**
     * Season Imp PAMs (Egypt mummy, Dark monk, Ice Age, Beach mermaid) are in
     * RESOURCES.json but not extracted. Use the tutorial Imp instead.
     * Imp Dragon is a different zombie and is not remapped here.
     */
    private String missingSeasonImpFallback(String key) {
        if (!looksLikeGenericImp(key)) {
            return null;
        }

        String tutorial = pamPaths.get("ZOMBIETUTORIALIMP");
        if (tutorial == null) {
            tutorial = pamPaths.get("GARGANTUARIMP");
        }
        if (tutorial == null) {
            tutorial = TUTORIAL_IMP_PAM;
        }

        if (!loggedMissingSeasonImp) {
            loggedMissingSeasonImp = true;
            Gdx.app.log(
                "ZombieAnimationResolver",
                "Season Imp PAM is not on disk (" + key
                    + "). Using tutorial Imp until egypt/monk/iceage/mermaid Imp PAMs are extracted."
            );
        }

        return tutorial;
    }

    private boolean looksLikeGenericImp(String key) {
        if (!key.contains("IMP")) {
            return false;
        }
        if (key.contains("DRAGON")
            || key.contains("IMPACT")
            || key.contains("IMPPEAR")
            || key.contains("ASH")
            || key.contains("SHOCK")) {
            return false;
        }
        return key.equals("ZOMBIEIMP")
            || key.equals("IMP")
            || key.contains("EGYPTIMP")
            || key.contains("ICEAGEIMP")
            || key.contains("BEACHIMP")
            || key.contains("IMPMONK")
            || key.contains("DARKIMP")
            || key.contains("TUTORIALIMP")
            || key.contains("PIRATEIMP");
    }

    private boolean looksLikeZomboss(String key) {
        return key.contains("ZOMBOSS")
            || (key.contains("BOSS")
            && (key.contains("EGYPT")
            || key.contains("ANCIENT")
            || key.contains("BEACH")
            || key.contains("DARK")
            || key.contains("FROZEN")
            || key.contains("ICEAGE")
            || key.contains("CAVE")));
    }

    private String findScannedZombossPath(String key) {
        String needle;
        if (key.contains("EGYPT") || key.contains("ANCIENT")) {
            needle = "EGYPTZOMBOSS";
        } else if (key.contains("BEACH") || key.contains("BIGWAVE")) {
            needle = "BEACHZOMBOSS";
        } else if (key.contains("DARK") || key.contains("AGES")) {
            needle = "DARKZOMBOSS";
        } else if (key.contains("FROZEN") || key.contains("ICEAGE") || key.contains("CAVE")) {
            needle = "ICEAGEZOMBOSS";
        } else {
            return null;
        }

        for (Map.Entry<String, String> entry : pamPaths.entrySet()) {
            String candidate = entry.getKey();
            String path = entry.getValue().replace('\\', '/').toUpperCase().replace("_", "");
            if (candidate.contains(needle) || path.contains(needle)) {
                return entry.getValue();
            }
        }

        return switch (needle) {
            case "EGYPTZOMBOSS" ->
                "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_ZOMBOSS/ZOMBIE_EGYPT_ZOMBOSS.PAM";
            case "BEACHZOMBOSS" ->
                "768/FULL/ZOMBIE/ZOMBIE_BEACH_ZOMBOSS/ZOMBIE_BEACH_ZOMBOSS.PAM";
            case "DARKZOMBOSS" ->
                "768/FULL/ZOMBIE/ZOMBIE_DARK_ZOMBOSS/ZOMBIE_DARK_ZOMBOSS.PAM";
            case "ICEAGEZOMBOSS" ->
                "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_ZOMBOSS/ZOMBIE_ICEAGE_ZOMBOSS.PAM";
            default -> null;
        };
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
                if (token.length() < 5 || "ZOMBIE".equals(token) || "BASIC".equals(token)) {
                    continue;
                }

                boolean matches = exact
                    ? token.equals(wantedToken)
                    : (wantedToken.length() >= 5
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
