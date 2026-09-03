package com.workshop.view.gameplay;

import com.workshop.controller.repository.Textures;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.ProjectileVisualVariant;

import java.util.HashMap;
import java.util.Map;

final class ProjectileLooks {

    private static final Map<String, String> cachedIds = new HashMap<>();

    private ProjectileLooks() {}

    static String imageId(Projectile projectile) {
        if (projectile == null) {
            return null;
        }
        return imageId(
            projectile.getOwnerPlant(),
            projectile.getVisualVariant(),
            projectile.getBulletType()
        );
    }

    static String imageId(
        Plant plant,
        ProjectileVisualVariant variant,
        BulletType bulletType
    ) {
        if (plant == null || plant.getName() == null) {
            return null;
        }

        String compact = ProjectilePamCatalog.compact(plant.getName());
        if (compact.isEmpty()) {
            return null;
        }

        String cacheKey = compact + "|"
            + (variant == null ? "DEFAULT" : variant.name())
            + "|"
            + (bulletType == null ? "NONE" : bulletType.name());
        if (cachedIds.containsKey(cacheKey)) {
            return cachedIds.get(cacheKey);
        }

        String[] candidates = candidatesFor(compact, variant, bulletType);
        for (String id : candidates) {
            if (id != null && Textures.regionOrNull(id) != null) {
                cachedIds.put(cacheKey, id);
                return id;
            }
        }

        return null;
    }

    static boolean isFirePeaLook(Projectile projectile) {
        if (projectile == null || projectile.getOwnerPlant() == null) {
            return false;
        }
        return isFirePeaLook(
            ProjectilePamCatalog.compact(projectile.getOwnerPlant().getName()),
            projectile.getBulletType()
        );
    }

    private static String[] candidatesFor(
        String compact,
        ProjectileVisualVariant variant,
        BulletType bulletType
    ) {
        boolean butter = variant == ProjectileVisualVariant.BUTTER;
        if (butter || compact.contains("KERNEL")) {
            if (butter) {
                return new String[]{
                    "IMAGE_EFFECTS_KERNELPULT_PROJECTILE_BUTTER",
                    "IMAGE_EFFECTS_T_KERNALPULT_PROJECTILE_T_KERNALPULT_PROJECTILE_35X37"
                };
            }
            return new String[]{
                "IMAGE_EFFECTS_T_KERNALPULT_PROJECTILE_T_KERNALPULT_PROJECTILE_35X37",
                "IMAGE_EFFECTS_T_KERNALPULT_PROJECTILE_T_KERNALPULT_PROJECTILE_30X75"
            };
        }

        if (compact.contains("WINTERMELON")) {
            return new String[]{
                "IMAGE_EFFECTS_T_WINTERMELON_PROJECTILE_T_WINTERMELON_PROJECTILE_135X95",
                "IMAGE_EFFECTS_T_WINTERMELON_PROJECTILE_T_WINTERMELON_PROJECTILE_85X59",
                "IMAGE_EFFECTS_T_WINTERMELON_PROJECTILE_T_WINTERMELON_PROJECTILE_87X60",
                "IMAGE_PLANT_WINTERMELON_WINTERMELON_122X83",
                "IMAGE_WINTERMELON_PROJECTILE"
            };
        }

        if (compact.contains("MELON")) {
            return new String[]{
                "IMAGE_EFFECTS_T_MELON_PROJECTILE_T_MELON_PROJECTILE_122X83",
                "IMAGE_EFFECTS_T_MELON_PROJECTILE_T_MELON_PROJECTILE_46X56",
                "IMAGE_PLANT_MELONPULT_MELONPULT_65X59"
            };
        }

        if (compact.contains("CABBAGE")) {
            return new String[]{
                "IMAGE_EFFECTS_T_CABBAGEPULT_PROJECTILE_T_CABBAGEPULT_PROJECTILE_98X103",
                "IMAGE_EFFECTS_T_CABBAGEPULT_PROJECTILE_T_CABBAGEPULT_PROJECTILE_125X117"
            };
        }

        if (compact.contains("PEPPER")) {
            return new String[]{
                "IMAGE_EFFECTS_T_PEPPERPULT_PROJECTILE_T_PEPPERPULT_PROJECTILE_55X61",
                "IMAGE_EFFECTS_T_PEPPERPULT_PROJECTILE_T_PEPPERPULT_PROJECTILE_133X138"
            };
        }

        if (compact.contains("SEASHROOM")) {
            return new String[]{
                "IMAGE_EFFECTS_SEASHROOM_PROJECTILE_SEASHROOM_PROJECTILE_36X35",
                "IMAGE_EFFECTS_SEASHROOM_PROJECTILE_SEASHROOM_PROJECTILE_45X45",
                "IMAGE_EFFECTS_SEASHROOM_PROJECTILE_SEASHROOM_PROJECTILE_43X33"
            };
        }

        if (compact.contains("PUFFSHROOM")) {
            return new String[]{
                "IMAGE_EFFECTS_T_PUFFSHROOM_PROJECTILE_T_PUFFSHROOM_PROJECTILE_23X22",
                "IMAGE_EFFECTS_T_PUFFSHROOM_PROJECTILE_T_PUFFSHROOM_PROJECTILE_22X21",
                "IMAGE_EFFECTS_T_PUFFSHROOM_PROJECTILE_T_PUFFSHROOM_PROJECTILE_16X16"
            };
        }

        if (compact.contains("FUMESHROOM")) {
            return new String[]{
                "IMAGE_EFFECTS_FUMESHROOM_BUBBLES_FUMESHROOM_BUBBLES_21X17",
                "IMAGE_EFFECTS_FUMESHROOM_BUBBLES_FUMESHROOM_BUBBLES_18X19",
                "IMAGE_EFFECTS_FUMESHROOM_BUBBLES_FUMESHROOM_BUBBLES_17X19",
                "IMAGE_EFFECTS_FUMESHROOM_BUBBLES_FUMESHROOM_BUBBLES_52X52"
            };
        }

        if (compact.contains("ROTOBAGA") || compact.contains("ROTORUTABAGA")) {
            return new String[]{
                "IMAGE_EFFECTS_T_ROTORUTABAGA_PROJECTILE1_T_ROTORUTABAGA_PROJECTILE1_29X28",
                "IMAGE_EFFECTS_T_ROTORUTABAGA_PROJECTILE1_T_ROTORUTABAGA_PROJECTILE1_18X18",
                "IMAGE_EFFECTS_T_ROTORUTABAGA_PROJECTILE1_T_ROTORUTABAGA_PROJECTILE1_18X31"
            };
        }

        if (compact.contains("CACTUS")) {
            return new String[]{
                "IMAGE_EFFECTS_T_CACTUS_PROJECTILE_T_CACTUS_PROJECTILE_52X35",
                "IMAGE_EFFECTS_CACTUS_PROJECTILE_CACTUS_PROJECTILE_52X35",
                "IMAGE_EFFECTS_T_CACTUS_PROJECTILE_T_CACTUS_PROJECTILE_70X32",
                "IMAGE_EFFECTS_T_CACTUS_PROJECTILE_T_CACTUS_PROJECTILE_11X11"
            };
        }

        if (compact.contains("SCAREDYSHROOM")) {
            return new String[]{
                "IMAGE_EFFECTS_SCAREDYSHROOM_PROJECTILE_SCAREDYSHROOM_PROJECTILE_45X45",
                "IMAGE_EFFECTS_SCAREDYSHROOM_PROJECTILE_SCAREDYSHROOM_PROJECTILE_88X38",
                "IMAGE_EFFECTS_SCAREDYSHROOM_PROJECTILE_SCAREDYSHROOM_PROJECTILE_128X47"
            };
        }

        if (isFirePeaLook(compact, bulletType)) {
            return new String[]{
                "IMAGE_EFFECTS_T_FIRE_PEA_T_FIRE_PEA_43X43",
                "IMAGE_EFFECTS_T_FIRE_PEA_T_FIRE_PEA_31X31",
                "IMAGE_EFFECTS_T_FIRE_PEA_T_FIRE_PEA_67X35"
            };
        }

        if (isPeaPlant(compact)) {
            return new String[]{
                "IMAGE_PLANT_" + compact + "_" + compact + "_23X23",
                "IMAGE_PLANT_PEASHOOTER_PEASHOOTER_23X23"
            };
        }

        return new String[0];
    }

    static float spriteScale(Plant plant) {
        if (plant == null || plant.getName() == null) {
            return 1f;
        }
        String compact = ProjectilePamCatalog.compact(plant.getName());
        if (compact.contains("MELON")) {
            return 1.7f;
        }
        if (compact.contains("CABBAGE") || compact.contains("PEPPER")) {
            return 1.4f;
        }
        if (compact.contains("FIREPEA")) {
            return 2.0f;
        }
        if (compact.contains("CACTUS")) {
            return 1.35f;
        }
        return 1f;
    }

    private static boolean isFirePeaLook(String compact, BulletType bulletType) {
        if (compact.contains("FIREPEA")) {
            return true;
        }
        if (bulletType != BulletType.FIRE || !isPeaPlant(compact)) {
            return false;
        }
        return !compact.contains("MELON") && !compact.contains("PEPPER");
    }

    private static boolean isPeaPlant(String compact) {
        return compact.contains("PEA")
            || compact.contains("REPEATER")
            || compact.contains("GATLING");
    }
}
