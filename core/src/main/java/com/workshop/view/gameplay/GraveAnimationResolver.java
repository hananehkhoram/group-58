package com.workshop.view.gameplay;

import com.workshop.model.season.Grave;


final class GraveAnimationResolver {

    private static final String CLIP_UNDAMAGED = "undamaged";
    private static final String CLIP_DAMAGE_1 = "damage1";
    private static final String CLIP_DAMAGE_2 = "damage2";
    private static final String CLIP_DAMAGE_3 = "damage3";
    private static final String CLIP_DAMAGE_4 = "damage4";

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

    static String getClip(int currentHp, int maxHp) {
        if (maxHp <= 0) {
            return CLIP_UNDAMAGED;
        }

        float fraction = (float) currentHp / maxHp;

        if (fraction > 0.8f) return CLIP_UNDAMAGED;
        if (fraction > 0.6f) return CLIP_DAMAGE_1;
        if (fraction > 0.4f) return CLIP_DAMAGE_2;
        if (fraction > 0.2f) return CLIP_DAMAGE_3;
        return CLIP_DAMAGE_4;
    }
}
