package com.workshop.view.gameplay;

import com.workshop.model.zombie.behavior.ArmorType;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Cone / bucket / brick from the tutorial zombie PAM, including damage stages. */
final class ZombieArmorVisibility {

    static final String OVERLAY_PAM =
        "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM";

    private static final Map<String, Set<String>> partNames = new HashMap<>();

    private ZombieArmorVisibility() {}

    static boolean usesPam(ArmorType type) {
        return type == ArmorType.CONE
            || type == ArmorType.BUCKET
            || type == ArmorType.BRICK;
    }

    static String partName(ArmorType type, int stage) {
        String prefix = switch (type) {
            case CONE -> "zombie_armor_cone_";
            case BUCKET -> "zombie_armor_bucket_";
            case BRICK -> "zombie_armor_brick_";
            default -> null;
        };
        if (prefix == null) {
            return null;
        }
        if (stage <= 0) {
            return prefix + "norm";
        }
        if (stage == 1) {
            return prefix + "damage_01";
        }
        return prefix + "damage_02";
    }

    static boolean pamHasPart(PamPlayer pamPlayer, String pamPath, String part) {
        if (pamPlayer == null || pamPath == null || part == null) {
            return false;
        }
        return names(pamPlayer, pamPath).contains(part);
    }

    static String overlayPam(PamPlayer pamPlayer, String bodyPam, String part) {
        if (pamHasPart(pamPlayer, bodyPam, part)) {
            return bodyPam;
        }
        if (pamHasPart(pamPlayer, OVERLAY_PAM, part)) {
            return OVERLAY_PAM;
        }
        return null;
    }

    private static Set<String> names(PamPlayer pamPlayer, String pamPath) {
        Set<String> cached = partNames.get(pamPath);
        if (cached != null) {
            return cached;
        }
        Set<String> found = new HashSet<>();
        PamPlayer.AnimationPart root = pamPlayer.getParts(pamPath);
        if (root != null) {
            collect(root, found);
        }
        partNames.put(pamPath, found);
        return found;
    }

    private static void collect(PamPlayer.AnimationPart part, Set<String> found) {
        if (part.name != null && !part.name.isEmpty()) {
            found.add(part.name);
        }
        if (part.children != null) {
            for (Object child : part.children) {
                collect((PamPlayer.AnimationPart) child, found);
            }
        }
    }
}
