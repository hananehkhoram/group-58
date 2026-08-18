package com.workshop.view.gameplay;

import com.workshop.model.season.Grave;

/**
 * PAM paths supplied directly (looked up via the PvZ Asset Browser / RESOURCES.json).
 * Clip: confirmed at runtime that gravestone PAMs expose
 * [undamaged, damage1, damage2, damage3, damage4] — a freshly-placed grave isn't
 * damaged yet, so "undamaged" is what should render by default.
 */
final class GraveAnimationResolver {

    private static final String CLIP = "undamaged";

    private static final String NORMAL_PAM =
        "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM";
    private static final String PLANT_FOOD_PAM =
        "768/FULL/GRAVESTONES/DARK_PLANTFOOD/DARK_PLANTFOOD.PAM";
    private static final String SUN_PAM =
        "768/FULL/GRAVESTONES/DARK_SUN/DARK_SUN.PAM";

    private GraveAnimationResolver() {}

    static String getPamPath(Grave.GraveType type) {
        switch (type) {
            case HAS_PLANT_FOOD: return PLANT_FOOD_PAM;
            case HAS_SUN: return SUN_PAM;
            default: return NORMAL_PAM;
        }
    }

    static String getClip(Grave.GraveType type) {
        return CLIP;
    }
}
