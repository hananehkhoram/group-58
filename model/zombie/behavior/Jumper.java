package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.plants.Tag;
import model.zombie.Zombie;

import java.util.EnumSet;
//
public class Jumper implements Behaviors {

    public enum JumpVariant {
        EXPLORER,    // مشعل‌دار — پرشی ندارد؛ منطق واقعی‌اش در Area است، این کلاس برایش no-op می‌ماند
        DODO,        // دودو سوار — از موانع خاص (نه هر گیاهی) می‌پرد
        PROSPECTOR,  // اکتشافگر — بعد از انفجار دینامیت (۱۰ ثانیه) به انتهای سطر پرتاب و برعکس می‌شود
        IMP,         // توسط Gargantuar پرتاب می‌شود؛ دقیقاً در ستون سوم از چپ فرود می‌آید
        DRAGON_IMP   // مثل IMP، فقط FireDamageMultiplier=0
    }

    private static final int TICKS_PER_SECOND = 10;
    private static final int HIGH_HP_OBSTACLE_THRESHOLD = 4000;

    private JumpVariant variant;
    private boolean landed;
    public boolean reverseTheWay;

    private int apex;
    private float timeToTravel;
    private int stunTime;
    private int targetColumn;

    private double startColumn;
    private long startTick;

    private double fireDamageMultiplier;

    public Jumper() {
        this.variant = JumpVariant.EXPLORER;
        this.landed = true;
        this.fireDamageMultiplier = 1.0;
    }

    public Jumper(float initialChance, float addChancePerGrid,
                  int minGridSquares, int maxGridSquares) {
        this.variant = JumpVariant.DODO;
        this.landed = true;
        this.fireDamageMultiplier = 1.0;
    }

    public Jumper(int apex, float timeToTravel, int stunTime, boolean reverseTheWay) {
        this.variant = JumpVariant.PROSPECTOR;
        this.apex = apex;
        this.timeToTravel = timeToTravel;
        this.stunTime = stunTime;
        this.reverseTheWay = reverseTheWay;
        this.landed = true;
        this.fireDamageMultiplier = 1.0;
    }

    /** Imp / DragonImp — پرتاب‌شده توسط Gargantuar */
    public Jumper(JumpVariant variant) {
        this.variant = variant;
        this.landed = true; // با پای زمین شروع می‌شه؛ فقط throwFrom() (توسط Gargantuar) بی‌زمینش می‌کنه
        this.fireDamageMultiplier = (variant == JumpVariant.DRAGON_IMP) ? 0.0 : 1.0;
    }

    public void throwFrom(GameContext ctx, Zombie zombie, double apex, double flightTime) {
        this.startColumn = zombie.getX();
        this.apex = (int) apex;
        this.timeToTravel = (float) flightTime;
        this.targetColumn = 2;
        this.startTick = ctx.getTimeManager().getTotalTicks();
        this.landed = false;
    }

    public void startJump(GameContext ctx, Zombie zombie, int targetCol, float flightSeconds, int apexValue) {
        this.startColumn = zombie.getX();
        this.targetColumn = targetCol;
        this.timeToTravel = flightSeconds;
        this.apex = apexValue;
        this.startTick = ctx.getTimeManager().getTotalTicks();
        this.landed = false;
    }

    public void land() { landed = true; }
    public void turnBackward() { reverseTheWay = true; }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (!landed) {
            advanceFlight(zombie, ctx);
            return;
        }

        if (variant == JumpVariant.DODO) {
            checkDodoObstacle(ctx, zombie);
        }
        // EXPLORER: بدون کاری — رفتار واقعی‌اش در Area پیاده می‌شود
        // PROSPECTOR: شروع پرش توسط LaserShooting (بعد از ۱۰ ثانیه انفجار دینامیت) صدا زده خواهد شد
    }

    private void advanceFlight(Zombie zombie, GameContext ctx) {
        long elapsedTicks = ctx.getTimeManager().getTotalTicks() - startTick;
        double elapsedSeconds = elapsedTicks / (double) TICKS_PER_SECOND;
        double progress = (timeToTravel <= 0) ? 1.0 : Math.min(1.0, elapsedSeconds / timeToTravel);

        zombie.setX(startColumn + (targetColumn - startColumn) * progress);

        if (progress >= 1.0) {
            zombie.setX(targetColumn);
            landed = true;
            if (variant == JumpVariant.PROSPECTOR && reverseTheWay) {
                zombie.setMovingBackward(true);
            }
        }
    }

    private void checkDodoObstacle(GameContext ctx, Zombie zombie) {
        int row = zombie.getRow();
        int aheadCol = (int) Math.floor(zombie.getX()) - 1;
        if (aheadCol < 0) return;

        Plant ahead = ctx.getPlantGrid()[row][aheadCol];
        if (!isObstacle(ahead)) return;

        int landingCol = Math.max(0, aheadCol - 1);
        startJump(ctx, zombie, landingCol, 0.6f, 40);
    }

    private boolean isObstacle(Plant p) {
        if (p == null || p.isDead()) return false;
        String name = p.getName() == null ? "" : p.getName().toLowerCase();
        if (name.contains("tall") && name.contains("nut")) return false;

        EnumSet<Tag> tags = p.getTags();
        if (tags != null && (tags.contains(Tag.MOVE_ZOMBIES) || tags.contains(Tag.TRAP))) return true;

        return p.getHp() >= HIGH_HP_OBSTACLE_THRESHOLD;
    }

    @Override
    public void onHit(Zombie zombie, int damage) {
        // DragonImp: مصونیت از آتش در GetDamage/Damage اعمال می‌شود (با استفاده از fireDamageMultiplier)
    }

    @Override
    public boolean isDestroyed() { return false; }

    public JumpVariant getVariant() { return variant; }
    public boolean isLanded() { return landed; }
    public int getApex() { return apex; }
    public int getStunTime() { return stunTime; }
    public int getTargetColumn() { return targetColumn; }
    public double getFireDamageMultiplier() { return fireDamageMultiplier; }
}