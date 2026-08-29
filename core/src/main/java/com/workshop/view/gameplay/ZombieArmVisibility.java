package com.workshop.view.gameplay;

import pvz.libpvz.pam.PamPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Outer-arm PAM layers keep their own names (zombie_arm_outer_*), but the
 * actual textured children are often IMAGE_* ids. Hiding the named outer-arm
 * groups drops those children. The stump bone is drawn separately.
 */
final class ZombieArmVisibility {

    private static final Map<String, Map<String, Boolean>> hideMaps = new HashMap<>();
    private static final Map<String, List<String>> armRoots = new HashMap<>();
    private static final Map<String, List<String>> stumpParts = new HashMap<>();

    private ZombieArmVisibility() {}

    static Map<String, Boolean> hideOuterArm(PamPlayer pamPlayer, String pamPath) {
        ensure(pamPlayer, pamPath);
        return hideMaps.get(pamPath);
    }

    static List<String> outerArmRoots(PamPlayer pamPlayer, String pamPath) {
        ensure(pamPlayer, pamPath);
        List<String> parts = armRoots.get(pamPath);
        return parts != null ? parts : List.of();
    }

    static List<String> stumpParts(PamPlayer pamPlayer, String pamPath) {
        ensure(pamPlayer, pamPath);
        List<String> parts = stumpParts.get(pamPath);
        return parts != null ? parts : List.of();
    }

    private static void ensure(PamPlayer pamPlayer, String pamPath) {
        if (pamPath == null || hideMaps.containsKey(pamPath)) {
            return;
        }
        Map<String, Boolean> hide = new HashMap<>();
        List<String> roots = new ArrayList<>();
        List<String> stumps = new ArrayList<>();
        PamPlayer.AnimationPart root = pamPlayer.getParts(pamPath);
        if (root != null) {
            collect(root, false, hide, roots, stumps);
        }
        if (roots.size() > 1) {
            String preferred = pickArmRoot(roots);
            if (preferred != null) {
                roots.clear();
                roots.add(preferred);
            }
        }
        hideMaps.put(pamPath, hide);
        armRoots.put(pamPath, roots);
        stumpParts.put(pamPath, stumps);
    }

    private static void collect(
        PamPlayer.AnimationPart part,
        boolean underOuterArm,
        Map<String, Boolean> hide,
        List<String> roots,
        List<String> stumps
    ) {
        String name = part.name;
        boolean outer = false;
        if (name != null) {
            String n = name.toLowerCase();
            if (isStump(n)) {
                stumps.add(name);
            } else if (isOuterArmGroup(n)) {
                outer = true;
                hide.put(name, false);
                if (!underOuterArm) {
                    roots.add(name);
                }
            } else if (n.contains("particle") && n.contains("arm")) {
                hide.put(name, false);
            }
        }
        if (part.children != null) {
            boolean childUnder = underOuterArm || outer;
            for (Object child : part.children) {
                collect((PamPlayer.AnimationPart) child, childUnder, hide, roots, stumps);
            }
        }
    }

    private static boolean isStump(String n) {
        return n.contains("arm_outer_upper_bone")
            || (n.contains("bone") && n.contains("outer") && n.contains("arm"));
    }

    private static boolean isOuterArmGroup(String n) {
        if (isStump(n) || n.contains("leg") || n.contains("foot") || n.contains("inner")) {
            return false;
        }
        boolean outer = n.contains("outer");
        boolean armOrHand = n.contains("arm") || n.contains("hand");
        return outer && armOrHand;
    }

    private static String pickArmRoot(List<String> roots) {
        for (String name : roots) {
            if (name.toLowerCase().contains("arms_outer")) {
                return name;
            }
        }
        for (String name : roots) {
            String n = name.toLowerCase();
            if (n.contains("arm_outer_upper") && !n.contains("bone")) {
                return name;
            }
        }
        return roots.get(0);
    }
}
