package model.zombie.behavior;

import model.zombie.Zombie;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * ZombieBeachSnorkel — swims underwater, surfacing only to eat specific plants.
 *
 * Key data from zombies.json:
 *   - TargetByIncludelist: plants it surfaces to attack (e.g. cherry_bomb, squash)
 *   - DamageWhileSubmerged: plants that can damage it even underwater
 *   - DamageWhileSubmergedPlantfoodOnly: plants that hit it only via Plant Food activation
 */
public class Submerge implements Behaviors {

    private boolean submerged;

    // Plants the snorkel will surface to eat
    private Set<String> targetPlants;

    // Plants whose projectiles penetrate underwater
    private Set<String> damageWhileSubmerged;

    // Plants that only hit it while submerged if activated by Plant Food
    private Set<String> damageWhileSubmergedPlantfoodOnly;

    public Submerge(List<String> targetPlants,
                    List<String> damageWhileSubmerged,
                    List<String> damageWhileSubmergedPlantfoodOnly) {
        this.submerged = true;
        this.targetPlants = new HashSet<>(targetPlants);
        this.damageWhileSubmerged = new HashSet<>(damageWhileSubmerged);
        this.damageWhileSubmergedPlantfoodOnly = new HashSet<>(damageWhileSubmergedPlantfoodOnly);
    }

    @Override
    public void onTick(Zombie zombie) {
        // Check if any targetPlant is in this lane — if yes, surface and eat
    }

    @Override
    public void onHit(Zombie zombie, int damage) {
        // Damage is ignored while submerged unless source is in damageWhileSubmerged
    }

    @Override
    public boolean isDestroyed() { return false; }

    public boolean isSubmerged() { return submerged; }
    public void surface() { submerged = false; }
    public void submerge() { submerged = true; }

    public boolean isVulnerableTo(String plantId, boolean isPlantFood) {
        if (!submerged) return true;
        if (damageWhileSubmerged.contains(plantId)) return true;
        if (isPlantFood && damageWhileSubmergedPlantfoodOnly.contains(plantId)) return true;
        return false;
    }

    public boolean willSurfaceFor(String plantId) {
        return targetPlants.contains(plantId);
    }
}