package model.zombie;

import model.zombie.behavior.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.zombie.behavior.ArmorType;

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

            //ARMOR ZOMBIES

            case "ZombieArmor1": // Cone Head
                behaviors.put("armor", new Armor(
                        ArmorType.CONE, ArmorType.CONE.baseHealth,
                        false, new double[]{0.666, 0.333}));
                break;

            case "ZombieArmor2": // Bucket Head
                behaviors.put("armor", new Armor(
                        ArmorType.BUCKET, ArmorType.BUCKET.baseHealth,
                        true, new double[]{0.666, 0.333}));
                break;

            case "ZombieArmor4": // Brick Head (unused in vanilla but in data)
                behaviors.put("armor", new Armor(
                        ArmorType.BRICK, ArmorType.BRICK.baseHealth,
                        false, new double[]{0.666, 0.333}));
                break;

            case "ZombieDarkArmor3":
                behaviors.put("armor", new Armor(          // کلاهخود — magnetshroom می‌تونه اینو بقاپه
                        ArmorType.SHOULDER_CROWN, ArmorType.SHOULDER_CROWN.baseHealth,
                        true, new double[]{0.666, 0.333}));
                behaviors.put("armor2", new Armor(         // شانه‌بند — magnetshroom نمی‌تونه اینو بقاپه
                        ArmorType.SHOULDER_ARMOR, ArmorType.SHOULDER_ARMOR.baseHealth,
                        true, new double[]{0.666, 0.333}));
                break;

            // NEWSPAPER

            case "ZombieNewspaper":
                behaviors.put("armor", new Armor(
                        ArmorType.NEWSPAPER, ArmorType.NEWSPAPER.baseHealth,
                        false, new double[]{0.5}, 4.0, 4.0));
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

            //  RA ZOMBIE

            case "ZombieRa":
                behaviors.put("sunThief", new SunThief(250, 1, 1));
                behaviors.put("getDamage", new GetDamage(GetDamage.GetDamageType.SUN_THIEF));
                break;

            // EXPLORER

            case "ZombieExplorer":
                behaviors.put("area", new Area(37,
                        List.of("frostbonnet", "blazingknight")));
                behaviors.put("jumper", new Jumper());
                break;

            // TOMB RAISER

            case "ZombieTombRaiser":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.TOMBRAISER, 0, 0));
                break;

            // ICE AGE

            case "ZombieIceAgeDodo":
                behaviors.put("jumper", new Jumper(0.05f, 0.04f, 1, 3));
                break;

            case "ZombieIceAgeHunter":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.HUNTER, 0, 0));
                break;

            case "ZombieIceAgeTroglobite":
                ActionBehavior.ActionParams trogParams = new ActionBehavior.ActionParams();
                trogParams.numberOfIceblocks = 3;
                trogParams.chillInsteadOfFreeze = true;
                behaviors.put("action", new ActionBehavior(ActionBehavior.ActionType.PUSH_GRID_ITEM, trogParams));
                break;

            // BEACH

            case "ZombieBeachFisherman":
                behaviors.put("shooting", new Shooting(Shooting.ShootingType.FISHERMAN, 0, 0));
                break;

            case "ZombieBeachOctopus":
                behaviors.put("action", new ActionBehavior(
                        ActionBehavior.ActionType.OCTOPUS_PROJECTILE, new ActionBehavior.ActionParams()));
                break;

            case "ZombieBeachSnorkel":
                behaviors.put("submerge", new Submerge(
                        List.of("ghostpepper","homingthistle","jalapeno","cherry_bomb","squash",
                                "iceburg","lavaguava","strawburst","electricblueberry","grapeshot",
                                "cabbagepult","akee","melonpult","kernelpult","wintermelon","banana",
                                "phatbeet","kiwibeast","sapfling","pepperpult","sporeshroom",
                                "shrinkingviolet","bloominghearts","dusklobber","applemortar",
                                "witchhazel","missiletoe","hotdate","tanglekelp","blastberry",
                                "blastspinner","frostbonnet","hammeruit","devourbloom"),
                        List.of("banana","ghostpepper","homingthistle","chomper","jalapeno",
                                "cherry_bomb","squash","iceburg","lavaguava","toadstool",
                                "strawburst","electricblueberry","grapeshot","jackolantern",
                                "cabbagepult","akee","melonpult","kernelpult","wintermelon",
                                "phatbeet","kiwibeast","sapfling","pepperpult","sporeshroom",
                                "shrinkingviolet","bloominghearts","dusklobber","applemortar",
                                "witchhazel","missiletoe","hotdate","tanglekelp","blastberry",
                                "solarsage","guardshroom","blastspinner","hammeruit"),
                        List.of("bowlingbulb","ghostpepper","homingthistle","chomper","fumeshroom",
                                "snapdragon","coconutcannon","bloomerang","spikeweed","spikerock",
                                "guacodile","laser_bean","firepeashooter","lavaguava","toadstool",
                                "cabbagepult","akee","melonpult","kernelpult","wintermelon","banana",
                                "phatbeet","kiwibeast","coldsnapdragon","sapfling","pepperpult",
                                "sporeshroom","shrinkingviolet","dandelion","applemortar","witchhazel",
                                "parsnip","missiletoe","shadowpeashooter","tanglekelp","ultomato",
                                "solarsage","frostbonnet","devourbloom","brainstem")));
                break;

            // DARK AGES

            case "ZombieDarkJuggler":
                behaviors.put("deflector", new ProjectileDeflector(
                        List.of("PeaDefault","ThreepeaterPeaDefault","FirePeaDefault",
                                "SnowPeaDefault","CannonballDefault","CabbageDefault",
                                "MelonDefault","WinterMelonDefault","KernelDefault","ButterDefault",
                                "StarFruitShot","BloomerangDefault","HomingThistleDefault",
                                "PepperpultDefault","AkeeDefault","GrapeshotDefaultProjectile"),
                        List.of("BuduhBoomDefaultProjectile","ButterDefault"),
                        List.of("HomingThistleDefault","HomingThistlePlantfood",
                                "BuduhBoomDefaultProjectile","ButterDefault"),
                        List.of(),
                        120,
                        2,
                        1000,
                        1.1,
                        160,
                        80,
                        0.9));
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

            case "ZombieModernAllStar": // با سرعت زیاد وارد میشه، اولین برخورد رو می‌کشه، بعدش خیلی آروم می‌شه
                behaviors.put("damage", new Damage(
                        List.of(Damage.TargetType.PLANT, Damage.TargetType.HYPNOTIZED_ZOMBIE),
                        0.2)); // TODO: ضریب دقیق کاهش سرعت طبق سند مشخص نیست؛ فعلاً تخمینی
                break;

            case "ZombieLostCityJane": // Umbrella Leaf zombie
                behaviors.put("deflector", new ProjectileDeflector(
                        ProjectileDeflector.DeflectMode.BOUNCE,
                        List.of("CabbageDefault","MegaCabbageDefault","KernelDefault",
                                "ButterDefault","MelonDefault","MegaMelonDefault",
                                "WinterMelonDefault","WinterMegaMelonDefault",
                                "PepperpultDefault","AkeeDefault","MegaAkeeDefault",
                                "SporeshroomDefault","BloomingHeartsDefault",
                                "DusklobberDefault","AppleMortarDefault",
                                "SlingPeaDefault","BuduhBoomDefaultProjectile",
                                "DragonBruitDefault","DragonBabyBruitDefault"),
                        160, 120, 0.9));
                break;

            case "ZombieArcade":
                ActionBehavior.ActionParams arcadeParams = new ActionBehavior.ActionParams();
                arcadeParams.jamStyle = "jam_8bit";
                behaviors.put("action", new ActionBehavior(ActionBehavior.ActionType.ARCADE_PUSH, arcadeParams));
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
                // Flips backward over the lane on a lit stick of dynamite
                behaviors.put("jumper", new Jumper(0, 0, 0, true));
                behaviors.put("laser", new LaserShooting(
                        LaserShooting.GunType.DYNAMITE, 0, 0, 0));
                break;

            case "ZombieParasol":
                behaviors.put("deflector", new ProjectileDeflector());
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