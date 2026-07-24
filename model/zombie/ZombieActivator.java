package model.zombie;

import model.zombie.behavior.*;
import model.zombie.behavior.ArmorType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZombieActivator {

    public static Map<String, Behaviors> buildBehaviors(Zombie zombie) {
        Map<String, Behaviors> behaviors = new HashMap<>();
        String id = zombie.getId();

        behaviors.put("eating", new Eating());

        switch (id) {

            // BASIC

            case "ZombieDefault":
                // No special behavior; just walks and eats
                break;

            // ARMOR ZOMBIES

            case "ZombieArmor1": // Cone Head
                behaviors.put("armor", new Armor(
                        ArmorType.CONE, ArmorType.CONE.baseHealth,
                        false, zombie.getX(), zombie.getY()));
                break;

            case "ZombieArmor2": // Bucket Head
                behaviors.put("armor", new Armor(
                        ArmorType.BUCKET, ArmorType.BUCKET.baseHealth,
                        true, zombie.getX(), zombie.getY()));
                break;

            case "ZombieArmor4": // Brick Head (unused in vanilla but in data)
                behaviors.put("armor", new Armor(
                        ArmorType.BRICK, ArmorType.BRICK.baseHealth,
                        false, zombie.getX(), zombie.getY()));
                break;

            case "ZombieDarkArmor3":
                behaviors.put("armor", new Armor(          // کلاهخود — magnetshroom می‌تونه اینو بقاپه
                        ArmorType.SHOULDER_CROWN, ArmorType.SHOULDER_CROWN.baseHealth,
                        true, zombie.getX(), zombie.getY()));
                behaviors.put("armor2", new Armor(         // شانه‌بند — magnetshroom نمی‌تونه اینو بقاپه
                        ArmorType.SHOULDER_ARMOR, ArmorType.SHOULDER_ARMOR.baseHealth,
                        true, zombie.getX(), zombie.getY()));
                break;

            // NEWSPAPER

            case "ZombieNewspaper":
                behaviors.put("armor", new Armor(
                        ArmorType.NEWSPAPER, ArmorType.NEWSPAPER.baseHealth,
                        false, zombie.getX(), zombie.getY()));
                break;

            // GARGANTUAR + IMP

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

            // RA ZOMBIE

            case "ZombieRa":
                behaviors.put("sunThief", new SunThief(250, 100, 5));
                break;

            // EXPLORER

            case "ZombieExplorer":
                behaviors.put("area", new Area());
                behaviors.put("jumper", new Jumper());
                break;

            // TOMB RAISER

            case "ZombieTombRaiser":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.TOMBRAISER, 2, 0));
                break;

            // ICE AGE

            case "ZombieIceAgeDodo":
                behaviors.put("jumper", new Jumper(0.05f, 0.04f, 1, 3));
                break;

            case "ZombieIceAgeHunter":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.HUNTER, 0, 0));
                break;

            case "ZombieIceAgeTroglobite":
                behaviors.put("pusher", new Pusher(Pusher.PushType.TROGLOBITE));
                break;

            // PIRATE / SEAS

            case "ZombieBarrelRoller":
                behaviors.put("pusher", new Pusher(Pusher.PushType.BARREL_ROLLER));
                break;

            // BEACH

            case "ZombieBeachFisherman":
                behaviors.put("area", new Area(Area.AreaType.FISHERMAN));
                break;

            case "ZombieBeachOctopus":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.OCTOPUS, 0, 0));
                break;

            case "ZombieBeachSnorkel":
                behaviors.put("submerge", new Submerge());
                break;

            // DARK AGES

            case "ZombieDarkJuggler":
                behaviors.put("deflector", new ProjectileDeflector(
                        ProjectileDeflector.DeflectMode.JUGGLE,
                        0, 0, 0));
                break;

            case "ZombieWizard":
                behaviors.put("action", new ActionBehavior(
                        ActionBehavior.ActionType.DARK_WIZARD_ZAP, new ActionBehavior.ActionParams()));
                break;

            case "ZombieDarkKing":
                ActionBehavior.ActionParams kingParams = new ActionBehavior.ActionParams();
                kingParams.delayBetweenKnightings = 2.5f;
                kingParams.knightingAreaX = 4;
                kingParams.knightingAreaY = 3;
                kingParams.validKnightTargets = List.of("dark","dark_armor1","dark_armor2","dark_armor3");
                kingParams.plantablePlants = List.of("cherry_bomb","jalapeno","powerlily",
                        "iceburg","empea","powerplant","goldleaf","grapeshot");
                behaviors.put("action", new ActionBehavior(ActionBehavior.ActionType.KNIGHT_KNIGHTING, kingParams));
                break;

            // MODERN DAY / LOST CITY / NEON MIXTAPE

            case "ZombieModernAllStar":
                behaviors.put("damage", new Damage(
                        List.of(Damage.TargetType.PLANT, Damage.TargetType.HYPNOTIZED_ZOMBIE),
                        0.2));
                break;

            case "ZombieLostCityJane": // Umbrella Leaf zombie
                behaviors.put("deflector", new ProjectileDeflector(
                        ProjectileDeflector.DeflectMode.BLOCK,
                        0, 0, 0.9));
                break;

            case "ZombieArcade":
                behaviors.put("pusher", new Pusher(Pusher.PushType.ARCADE));
                break;

            // ANCIENT EGYPT SPECIAL

            case "ZombieCrystalSkull":
                behaviors.put("laser", new LaserShooting(
                        5,
                        0.2,
                        4001,
                        220,
                        5));
                break;

            case "ZombieProspector":
                behaviors.put("jumper", new Jumper(0, 0, 0, true));
                behaviors.put("laser", new LaserShooting(
                        LaserShooting.GunType.DYNAMITE, 0, 0, 0));
                break;

            // NEON MIXTAPE

            case "ZombiePiano":
                behaviors.put("pianoCharge", new PianoCharge(
                        0.4,
                        0.12,
                        4000,
                        3,
                        2,
                        List.of("spikeweed","spikerock","cactus","iceweed")));
                break;

            default:
                break;
        }

        return behaviors;
    }
}