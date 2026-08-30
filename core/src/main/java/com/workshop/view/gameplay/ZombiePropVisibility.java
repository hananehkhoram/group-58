package com.workshop.view.gameplay;

import com.workshop.model.zombie.Zombie;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Explorer has unattached clothing/torch image slices that draw at the
 * canvas rest pose above the head. There is no separately named hat part
 * in this PAM; do not overlay a guessed atlas sprite.
 */
final class ZombiePropVisibility {

    private static final String[] HIDDEN_EXPLORER_PARTS = {
        "zombie_explorer_27x127",
        "zombie_explorer_159x106",
        "zombie_explorer_24x49",
        "zombie_explorer_26x41",
        "zombie_explorer_16x39",
        "zombie_explorer_37x24",
        "zombie_explorer_43x25",
        "zombie_explorer_34x31",
        "zombie_explorer_70x71",
        "zombie_explorer_54x78",
        "zombie_explorer_152x104",
        "zombie_explorer_153x106",
        "zombie_explorer_154x104",
        "zombie_explorer_154x106",
        "torch_fire_fire_frame_01",
        "torch_fire_frame_02"
    };

    private ZombiePropVisibility() {}

    static Map<String, Boolean> visibility(
        PamPlayer pamPlayer,
        String pamPath,
        Zombie zombie
    ) {
        if (pamPlayer == null || pamPath == null || !isExplorer(pamPath, zombie)) {
            return null;
        }

        PamPlayer.AnimationPart root = pamPlayer.getParts(pamPath);
        if (root == null) {
            return null;
        }

        Map<String, Boolean> visibility = new HashMap<>();
        collect(root, visibility);
        return visibility.isEmpty() ? null : visibility;
    }

    private static void collect(
        PamPlayer.AnimationPart part,
        Map<String, Boolean> visibility
    ) {
        String name = part.name;
        if (name != null && !name.isEmpty()) {
            String n = name.toLowerCase(Locale.ROOT);
            if (shouldHideExplorerPart(n)) {
                visibility.put(name, false);
            }
        }
        if (part.children == null) {
            return;
        }
        for (Object child : part.children) {
            collect((PamPlayer.AnimationPart) child, visibility);
        }
    }

    private static boolean shouldHideExplorerPart(String n) {
        for (String hidden : HIDDEN_EXPLORER_PARTS) {
            if (n.contains(hidden)) {
                return true;
            }
        }
        return n.contains("torch")
            || n.contains("flame")
            || (n.contains("fire") && !n.contains("fireman"));
    }

    private static boolean isExplorer(String pamPath, Zombie zombie) {
        String path = pamPath.replace('\\', '/').toUpperCase(Locale.ROOT);
        if (path.contains("EXPLORER")) {
            return true;
        }
        if (zombie == null) {
            return false;
        }
        String name = zombie.getName();
        String id = zombie.getId();
        return name != null && name.toLowerCase(Locale.ROOT).contains("explorer")
            || id != null && id.toLowerCase(Locale.ROOT).contains("explorer");
    }
}
