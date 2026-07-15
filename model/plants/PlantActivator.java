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

/**
 * Bridges Plant.abilityParams (raw String -> String map loaded from CSV) to
 * each ability class's strongly-typed method call.
 *
 * This is the pattern to call from your game loop (e.g. inside
 * Plant.update(GameContext ctx), once per actionInterval tick):
 *
 *   PlantActivator.activate(plant, ctx);
 *
 * and on Plant Food pickup:
 *
 *   PlantActivator.activatePlantFood(plant, ctx);
 *
 * HOW TO READ THIS CLASS:
 * 1. instanceof tells you WHICH method to call (Shooters -> shoot(), Lobber -> lob(), ...).
 * 2. plant.getAbilityParams() tells you WHAT VALUES to pass — it's a
 *    Map<String,String> where the keys are exactly the param names you'll
 *    see for that plant's row in plants.csv's "abilityParams" column
 *    (e.g. Repeater's row has "amount=2;shootType=STRAIGHT_SEQUENTIAL;bulletType=NORMAL").
 * 3. Parse each String value into the real enum/int the method wants, then call it.
 *
 * row/col/time-style runtime arguments (Explosive.explosion, MeleeAttackers.melee)
 * are NOT in the CSV — those come from the plant's own state (plant.getRow()/getCol())
 * or the board/game clock at the moment of activation, since they depend on
 * where the plant is planted and when it fires, not on which plant it is.
 */
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
            String interval = p.get("interval");
            String damage = p.get("damage");
            shooters.shoot(damage, amount, interval, shootType, bulletType, plant, engine);
            if (shootType == ShootType.RANDOM_HOMING || shootType == ShootType.NEAREST_TARGET) {
                shooters.shootForHoming();
            }

        } else if (ability instanceof Lobber lobber) {
            LobType lobType = LobType.valueOf(p.get("lobType"));
            String interval = p.get("interval");
            lobber.lob(lobType, interval, plant, ctx);

        } else if (ability instanceof Explosive explosive) {
            ExplosiveType type = ExplosiveType.valueOf(p.get("explosiveType"));
            String damageString = p.get("Damage");
            int damage = Integer.parseInt(p.get("damage"));
            explosive.triggerAbility(type, damage, plant, engine);
            // row/col/time are NOT csv-driven — they come from where this plant
            // is planted and the game clock, e.g.:
            // explosive.explosion(ctx.getCurrentTick(), plant.getRow(), plant.getCol());


        } else if (ability instanceof MeleeAttackers melee) {
            String meleeKind = p.get("meleeKind");

            if ("INSTANT_EAT".equals(meleeKind)) {
                melee.instantEat(plant, engine);
            } else {
                int damage = Integer.parseInt(p.get("damage"));
                melee.melee(meleeKind, damage, plant, engine);
            }

        } else if (ability instanceof WallNut wallNut) {
            WallNutType wallNutType = WallNutType.valueOf(p.get("wallNutType"));
            String damageString = p.get("Damage");
            int damage = Integer.parseInt(p.get("damage"));
            wallNut.triggerAbility(wallNutType, damage, plant, engine);
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

    public static void activatePlantFood(Plant plant, GameContext ctx) {
        // activatePlantFood() is a default no-op on BaseAbility unless overridden;
        // each class reads plant.getPlantFoodMode() itself to decide how to
        // amplify its own behavior (see e.g. Shooters.activatePlantFood).
        plant.activatePlantFood(ctx);
    }
}
