package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

/**
 * Base ability for plants whose effect doesn't fit Shooters, Lobber,
 * Explosive, MeleeAttackers, WallNut, or SunProducers: bullet-property
 * changers, hypnotizers, copiers, and planting-surface providers
 * (Torchwood, Hypno-shroom, Imitater, Lily Pad).
 */
public class Modifier implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {

    }

    public void modify(ModifierType modifierType) {
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // Effect varies per plant: blue flame buff, gargantuar conversion, self-cloning, etc.
    }
}
