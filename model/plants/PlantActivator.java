package model.plants;

import model.GameContext;
import model.mechanisms.GameEngine;
import model.mechanisms.SunType;
import model.projectile.BulletType;
import model.plants.enums.ShootType;
import model.plants.plantAbilities.*;
import model.zombie.behavior.Damage;

import java.util.Date;
import java.util.Map;

public final class PlantActivator {

    private PlantActivator() {
    }

    public static void activate(Plant plant, GameContext ctx, GameEngine engine) {
        BaseAbility ability = plant.getBaseAbility();
        Map<String, String> p = plant.getAbilityParams();

        if (ability instanceof Shooters shooters) {
            int amount = Integer.parseInt(p.get("amount"));
            ShootType shootType = ShootType.valueOf(p.get("shootType"));
            BulletType bulletType = BulletType.valueOf(p.get("bulletType"));
            String interval = actionIntervalAsWholeSeconds(plant);
            String damage = String.valueOf(parseBaseDamage(plant));
            shooters.shoot(damage, amount, interval, shootType, bulletType, plant, engine);

        } else if (ability instanceof Lobber lobber) {
            LobType lobType = LobType.valueOf(p.get("lobType"));
            String interval = actionIntervalAsWholeSeconds(plant);
            lobber.lob(lobType, interval, plant, ctx);

        } else if (ability instanceof Explosive explosive) {
            ExplosiveType type = ExplosiveType.valueOf(p.get("explosiveType"));
            int damage = parseBaseDamage(plant);
            explosive.triggerAbility(type, damage, plant, engine);
            // explosive.explosion(ctx.getCurrentTick(), plant.getRow(), plant.getCol());


        } else if (ability instanceof MeleeAttackers melee) {
            String meleeKind = p.get("meleeKind");

            if ("INSTANT_EAT".equals(meleeKind)) {
                melee.instantEat(plant, engine);
            } else {
                int damage = parseBaseDamage(plant);
                melee.melee(meleeKind, damage, plant, engine);
            }

        } else if (ability instanceof WallNut wallNut) {
            WallNutType wallNutType = WallNutType.valueOf(p.get("wallNutType"));
            wallNut.wall(wallNutType, plant, ctx);

        } else if (ability instanceof SunProducers sunProducers) {
            String rate = p.get("sunRate"); // "24", "0", or "everyRound" — passed through as-is
            int amount = Integer.parseInt(p.get("sunAmount"));
            SunType sunType = SunType.valueOf(p.get("sunType"));
            sunProducers.produceSun(rate, amount, sunType, ctx, plant);

        } else if (ability instanceof Modifier modifier) {
            ModifierType modifierType = ModifierType.valueOf(p.get("modifierType"));
            modifier.modify(modifierType, plant, engine);

        } else if (ability instanceof PlantFooder) {
            // No extra params: PlantFooder's whole job IS the plant-food effect,
            // triggered via activatePlantFood() below, not activate().
        }
    }

    private static String actionIntervalAsWholeSeconds(Plant plant) {
        Double interval = plant.getActionInterval();
        if (interval == null) {
            throw new IllegalStateException(
                    "Plant '" + plant.getName() + "' has no actionInterval but its ability needs one.");
        }
        return String.valueOf(Math.round(interval));
    }

    private static int parseBaseDamage(Plant plant) {
        String raw = plant.getDamage();
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Plant '" + plant.getName() + "' has no damage value.");
        }
        raw = raw.trim();

        if (raw.equalsIgnoreCase("Insta-kill")) {
            return Integer.MAX_VALUE;
        }
        if (raw.contains("x")) {
            raw = raw.substring(0, raw.indexOf('x'));
        }
        if (raw.contains("/")) {
            String[] levels = raw.split("/");
            int idx = Math.max(0, Math.min(plant.getLevel() - 1, levels.length - 1));
            raw = levels[idx];
        }
        return Integer.parseInt(raw.trim());
    }

    public static void activatePlantFood(Plant plant, GameContext ctx) {
        // activatePlantFood() is a default no-op on BaseAbility unless overridden;
        // each class reads plant.getPlantFoodMode() itself to decide how to
        // amplify its own behavior (see e.g. Shooters.activatePlantFood).
        plant.activatePlantFood(ctx);
    }
}