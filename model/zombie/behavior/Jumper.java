package model.zombie.behavior;

import model.zombie.Zombie;

public class Jumper implements Behaviors {

    public enum JumpVariant {
        EXPLORER,    // vaults over plants it can't eat
        DODO,        // random jump per grid walked
        PROSPECTOR,  // fixed arc, reverses direction on land
        IMP,         // thrown by Gargantuar; lands at target column
        DRAGON_IMP   // same arc as IMP but immune to fire (FireDamageMultiplier=0)
    }

    private JumpVariant variant;
    private boolean landed;
    public boolean reverseTheWay; // Prospector: walks backward after landing

    // Arc params
    private int apex;
    private float timeToTravel;
    private int stunTime;        // Prospector: stun on landing
    private int targetColumn;    // Imp: ImpTargetColumn=2

    // Dodo random-jump params
    private float addRandomChanceForJumpPerGrid;   // 0.04
    private float initialSetRandomChanceForJump;   // 0.05
    private int minGridSquaresToFlyOver;           // 1
    private int maxGridSquaresToFlyOver;           // 3

    // Imp fire-immunity
    private double fireDamageMultiplier;

    public Jumper() {
        this.variant = JumpVariant.EXPLORER;
        this.landed = true;
        this.fireDamageMultiplier = 1.0;
    }

    /** Dodo */
    public Jumper(float initialChance, float addChancePerGrid,
                  int minGridSquares, int maxGridSquares) {
        this.variant = JumpVariant.DODO;
        this.initialSetRandomChanceForJump = initialChance;
        this.addRandomChanceForJumpPerGrid = addChancePerGrid;
        this.minGridSquaresToFlyOver = minGridSquares;
        this.maxGridSquaresToFlyOver = maxGridSquares;
        this.landed = true;
        this.fireDamageMultiplier = 1.0;
    }

    /** Prospector */
    public Jumper(int apex, float timeToTravel, int stunTime, boolean reverseTheWay) {
        this.variant = JumpVariant.PROSPECTOR;
        this.apex = apex;
        this.timeToTravel = timeToTravel;
        this.stunTime = stunTime;
        this.reverseTheWay = reverseTheWay;
        this.landed = true;
        this.fireDamageMultiplier = 1.0;
    }

    /** Imp / DragonImp — thrown by Gargantuar */
    public Jumper(JumpVariant variant) {
        this.variant = variant;
        // DragonImp starts on the ground (rides dragon); standard Imp starts mid-flight
        this.landed = (variant == JumpVariant.DRAGON_IMP);
        this.fireDamageMultiplier = (variant == JumpVariant.DRAGON_IMP) ? 0.0 : 1.0;
    }


    /** Called by Gargantuar when it throws this Imp. */
    public void throwFrom(double apex, double flightTime, int targetColumn) {
        this.apex = (int) apex;
        this.timeToTravel = (float) flightTime;
        this.targetColumn = targetColumn;
        this.landed = false;
    }

    public void land() { landed = true; }
    public void turnBackward() { reverseTheWay = true; }
    public void jump(int x, int y) {}

    @Override
    public void onTick(Zombie zombie) {}

    @Override
    public void onHit(Zombie zombie, int damage) {
        // DragonImp: scale fire damage down to 0
        if (variant == JumpVariant.DRAGON_IMP) {
        }
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