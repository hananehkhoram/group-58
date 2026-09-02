package com.workshop.model.zombie;

import com.workshop.model.zombie.behavior.Armor;
import com.workshop.model.zombie.behavior.Behaviors;
import com.workshop.model.zombie.behavior.Jumper;
import com.workshop.model.zombie.behavior.ProjectileDeflector;
import com.workshop.model.zombie.behavior.Submerge;
import com.workshop.model.zombie.behavior.ZombossSummon;

import java.util.Map;

/**
 * Behavior map lookups shared by {@link Zombie}.
 */
final class ZombieBehaviorAccess {

    private ZombieBehaviorAccess() {}

    static Jumper jumper(Map<String, Behaviors> behaviors) {
        Behaviors b = behaviors.get("jumper");
        return (b instanceof Jumper) ? (Jumper) b : null;
    }

    static ProjectileDeflector deflector(Map<String, Behaviors> behaviors) {
        Behaviors b = behaviors.get("deflector");
        return (b instanceof ProjectileDeflector) ? (ProjectileDeflector) b : null;
    }

    static ZombossSummon zomboss(Map<String, Behaviors> behaviors) {
        if (behaviors == null) {
            return null;
        }
        Behaviors b = behaviors.get("zombossSummon");
        return (b instanceof ZombossSummon) ? (ZombossSummon) b : null;
    }

    static Submerge submerge(Map<String, Behaviors> behaviors) {
        Behaviors b = behaviors.get("submerge");
        return (b instanceof Submerge) ? (Submerge) b : null;
    }

    static Armor armor(Map<String, Behaviors> behaviors) {
        Behaviors b = behaviors.get("armor");
        return (b instanceof Armor) ? (Armor) b : null;
    }

    static Armor secondaryArmor(Map<String, Behaviors> behaviors) {
        Behaviors b = behaviors.get("armor2");
        return (b instanceof Armor) ? (Armor) b : null;
    }

    /**
     * @return remaining damage after armor absorption
     */
    static int absorbThroughArmor(Zombie zombie, int remaining, ZombieInjuryVisuals injury) {
        Map<String, Behaviors> behaviors = zombie.getBehaviors();
        Armor primary = armor(behaviors);
        if (primary != null && !primary.isDestroyed()) {
            remaining = primary.absorb(remaining);
            if (primary.isDestroyed()) {
                injury.queueArmorPop(primary.getArmorType());
            }
            if (remaining <= 0) {
                return 0;
            }
        }
        Armor secondary = secondaryArmor(behaviors);
        if (secondary != null && !secondary.isDestroyed()) {
            remaining = secondary.absorb(remaining);
            if (secondary.isDestroyed()) {
                injury.queueArmorPop(secondary.getArmorType());
            }
            if (remaining <= 0) {
                return 0;
            }
        }
        return remaining;
    }

    static Armor removeArmor(Zombie zombie) {
        Map<String, Behaviors> behaviors = zombie.getBehaviors();
        Armor primary = armor(behaviors);
        if (primary != null && !primary.isDestroyed()) {
            primary.afterDestroy(zombie);
            behaviors.remove("armor");
            return primary;
        }
        Armor secondary = secondaryArmor(behaviors);
        if (secondary != null && !secondary.isDestroyed()) {
            secondary.afterDestroy(zombie);
            behaviors.remove("armor2");
            return secondary;
        }
        return null;
    }
}
