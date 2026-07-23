package model.zombie.behavior;

import model.GameContext;
import model.level.Level;
import model.plants.Plant;
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
        this.targetPlants = normalizeAll(targetPlants);
        this.damageWhileSubmerged = normalizeAll(damageWhileSubmerged);
        this.damageWhileSubmergedPlantfoodOnly = normalizeAll(damageWhileSubmergedPlantfoodOnly);
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int col = (int) zombie.getX();
        int totalRows = Level.ROWS;
        int totalCols = Level.COLS;

        Plant target = (row >= 0 && row < totalRows && col >= 0 && col < totalCols)
                ? ctx.getPlantGrid()[row][col] : null;
        boolean hasLiveTarget = target != null && !target.isDead();

        if (submerged) {
            if (hasLiveTarget && willSurfaceFor(target.getName())) {
                surface();
            }
        } else if (!hasLiveTarget) {
            submerge();
        }
    }

    @Override
    public void onHit(Zombie zombie, int damage) {
        // مسیر واقعی اعمال دمیج (Projectile.onHit → Zombie.takeDamage) هنوز اصلاً از
        // isVulnerableTo() اینجا خبر نداره — این قسمت فعلاً هیچ اثری روی جونش نداره.
        // باید قبل از takeDamage، منبع پرتابه/گیاه چک بشه (شبیه همون مشکلی که با
        // ProjectileDeflector داشتیم: کلاس آماده‌ست ولی جایی صداش نمی‌زنه).
    }

    @Override
    public boolean isDestroyed() { return false; }

    public boolean isSubmerged() { return submerged; }
    public void surface() { submerged = false; }
    public void submerge() { submerged = true; }

    public boolean isVulnerableTo(String plantId, boolean isPlantFood) {
        if (!submerged) return true;
        String key = normalize(plantId);
        if (damageWhileSubmerged.contains(key)) return true;
        if (isPlantFood && damageWhileSubmergedPlantfoodOnly.contains(key)) return true;
        return false;
    }

    public boolean willSurfaceFor(String plantId) {
        return targetPlants.contains(normalize(plantId));
    }

    // Plant.getName() ("Cherry Bomb", "Melon-pult") با فرمت این لیست‌ها ("cherry_bomb", "melonpult")
    // یکی نیست؛ برای مقایسه‌ی درست، هردو طرف رو به یه شکل ساده (حروف کوچیک، بدون فاصله/خط‌تیره/زیرخط) می‌بریم.
    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[\\s\\-_]", "");
    }

    private static Set<String> normalizeAll(List<String> raw) {
        Set<String> result = new HashSet<>();
        for (String s : raw) result.add(normalize(s));
        return result;
    }
}