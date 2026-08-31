package com.workshop.model.zombie;

import com.workshop.model.zombie.behavior.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZombieActivator {

    public static Map<String, Behaviors> buildBehaviors(Zombie zombie) {
        Map<String, Behaviors> behaviors = new HashMap<>();
        String id = zombie.getId();

        behaviors.put("eating", new Eating());

        applyArmorBehaviors(id, zombie, behaviors);
        applyGargantuarAndImpBehaviors(id, behaviors);
        applyEgyptBehaviors(id, behaviors);
        applyIceAgeBehaviors(id, behaviors);
        applyWaterAndPirateBehaviors(id, behaviors);
        applyDarkAgesBehaviors(id, behaviors);
        applyModernAndMiscBehaviors(id, behaviors);
        applyZombotanyBehaviors(id, behaviors);
        applyBossBehaviors(id, behaviors);

        return behaviors;
    }

    private static void applyBossBehaviors(String id, Map<String, Behaviors> behaviors) {
        if (BossZombieRegistry.isBossId(id)) {
            behaviors.put("zombossSummon", new ZombossSummon());
        }
    }

    private static void applyArmorBehaviors(String id, Zombie zombie, Map<String, Behaviors> behaviors) {
        float x = (float) zombie.getX();
        float y = (float) zombie.getY();

        switch (id) {
            case "ZombieArmor1": // Cone Head
                behaviors.put("armor", new Armor(ArmorType.CONE, ArmorType.CONE.baseHealth, false, x, y));
                break;
            case "ZombieArmor2": // Bucket Head
                behaviors.put("armor", new Armor(ArmorType.BUCKET, ArmorType.BUCKET.baseHealth, true, x, y));
                break;
            case "ZombieArmor4": // Brick Head
                behaviors.put("armor", new Armor(ArmorType.BRICK, ArmorType.BRICK.baseHealth, false, x, y));
                break;
            case "ZombieDarkArmor3":
                behaviors.put("armor", new Armor(ArmorType.SHOULDER_CROWN, ArmorType.SHOULDER_CROWN.baseHealth, true, x, y));
                behaviors.put("armor2", new Armor(ArmorType.SHOULDER_ARMOR, ArmorType.SHOULDER_ARMOR.baseHealth, true, x, y));
                break;
            case "ZombieNewspaper":
                behaviors.put("armor", new Armor(ArmorType.NEWSPAPER, ArmorType.NEWSPAPER.baseHealth, false, x, y));
                break;
        }
    }

    private static void applyGargantuarAndImpBehaviors(String id, Map<String, Behaviors> behaviors) {
        switch (id) {
            case "ZombieGargantuar":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.GARGANTUAR, 0, 1));
                behaviors.put("damage", new Damage());
                break;
            case "ZombieImp":
                behaviors.put("jumper", new Jumper(Jumper.JumpVariant.IMP));
                break;
            case "ZombieDarkImpDragon":
                behaviors.put("jumper", new Jumper(Jumper.JumpVariant.DRAGON_IMP));
                break;
        }
    }

    private static void applyEgyptBehaviors(String id, Map<String, Behaviors> behaviors) {
        switch (id) {
            case "ZombieRa":
                behaviors.put("sunThief", new SunThief(250, 100, 5));
                break;
            case "ZombieExplorer":
                behaviors.put("area", new Area());
                behaviors.put("jumper", new Jumper());
                break;
            case "ZombieTombRaiser":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.TOMBRAISER, 2, 0));
                break;
            case "ZombieCrystalSkull":
                behaviors.put("laser", new LaserShooting(5, 0.2, 4001, 220, 5));
                break;
        }
    }

    private static void applyIceAgeBehaviors(String id, Map<String, Behaviors> behaviors) {
        switch (id) {
            case "ZombieIceAgeDodo":
                behaviors.put("jumper", new Jumper(0.05f, 0.04f, 1, 3));
                break;
            case "ZombieIceAgeHunter":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.HUNTER, 0, 0));
                break;
            case "ZombieIceAgeTroglobite":
                behaviors.put("pusher", new Pusher(Pusher.PushType.TROGLOBITE));
                break;
        }
    }

    private static void applyWaterAndPirateBehaviors(String id, Map<String, Behaviors> behaviors) {
        switch (id) {
            case "ZombieBarrelRoller":
                behaviors.put("pusher", new Pusher(Pusher.PushType.BARREL_ROLLER));
                break;
            case "ZombieBeachFisherman":
                behaviors.put("area", new Area(Area.AreaType.FISHERMAN));
                break;
            case "ZombieBeachOctopus":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.OCTOPUS, 0, 0));
                break;
            case "ZombieBeachSnorkel":
                behaviors.put("submerge", new Submerge());
                break;
        }
    }

    private static void applyDarkAgesBehaviors(String id, Map<String, Behaviors> behaviors) {
        switch (id) {
            case "ZombieDarkJuggler":
                behaviors.put("deflector", new ProjectileDeflector(ProjectileDeflector.DeflectMode.JUGGLE, 0, 0, 0));
                break;
            case "ZombieWizard":
                ActionBehavior.ActionParams wizardParams = new ActionBehavior.ActionParams();
                wizardParams.zapDelay = 3.0f;
                behaviors.put("action", new ActionBehavior(ActionBehavior.ActionType.DARK_WIZARD_ZAP, wizardParams));
                break;
            case "ZombieDarkKing":
                ActionBehavior.ActionParams kingParams = new ActionBehavior.ActionParams();
                kingParams.delayBetweenKnightings = 2.5f;
                kingParams.knightingAreaX = 4;
                kingParams.knightingAreaY = 3;
                behaviors.put("action", new ActionBehavior(ActionBehavior.ActionType.KNIGHT_KNIGHTING, kingParams));
                break;
        }
    }

    private static void applyModernAndMiscBehaviors(String id, Map<String, Behaviors> behaviors) {
        switch (id) {
            case "ZombieModernAllStar":
                behaviors.put("damage", new Damage(List.of(Damage.TargetType.PLANT, Damage.TargetType.HYPNOTIZED_ZOMBIE), 0.2));
                break;
            case "ZombieLostCityJane": // Umbrella Leaf zombie
                behaviors.put("deflector", new ProjectileDeflector(ProjectileDeflector.DeflectMode.BLOCK, 0, 0, 0.9));
                break;
            case "ZombieArcade":
                behaviors.put("pusher", new Pusher(Pusher.PushType.ARCADE));
                break;
            case "ZombieProspector":
                behaviors.put("jumper", new Jumper(0, 0, 0, true));
                behaviors.put("laser", new LaserShooting(LaserShooting.GunType.DYNAMITE, 0, 0, 0));
                break;
            case "ZombiePiano":
                behaviors.put("pianoCharge", new PianoCharge(0.4, 0.12, 4000, 3, 2, List.of("spikeweed", "spikerock", "cactus", "iceweed")));
                break;
        }
    }

    private static void applyZombotanyBehaviors(
        String id,
        Map<String, Behaviors> behaviors) {

        switch (id) {
            case "ZombieZombotanyPeashooter":
                behaviors.put(
                    "shooting",
                    new Shooting(
                        Shooting.ShootingType.PEASHOOTER,
                        0,
                        0
                    )
                );
                break;

            case "ZombieZombotanyJalapeno":
                behaviors.put(
                    "jalapenoExplosion",
                    new JalapenoExplosion()
                );
                break;

            case "ZombieZombotanySquash":
                behaviors.put(
                    "squashAttack",
                    new SquashAttack()
                );
                break;

            default:
                break;
        }
    }
}
