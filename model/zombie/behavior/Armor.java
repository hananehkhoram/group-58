package model.zombie.behavior;

import model.GameContext;
import model.mechanisms.Tile;
import model.plants.Plant;
import model.zombie.Zombie;

public class Armor implements Behaviors {

    private int armorHP;
    private ArmorType armorType;
    private boolean metallic;

    // Enrage fields (Newspaper zombie: EnragedDamageScale=4, EnragedSpeedScale=4)
    private boolean enrageable;
    private double enragedDamageScale;
    private double enragedSpeedScale;
    private boolean enraged;

    private double x, y;
    private boolean isMagetized = false;
    private double vx, vy;

    public void setMagetized(boolean isMagetized) {
        this.isMagetized = isMagetized;
    }

    public boolean isMagetized() {
        return isMagetized;
    }

    public Armor(ArmorType armorType, int armorHP, boolean metallic, double x, double y) {
        this.armorType = armorType;
        this.armorHP = armorHP;
        this.metallic = metallic;
        this.enrageable = false;
        this.x = x;
        this.y = y;
    }

    /** Constructor for Newspaper zombie. */
    public Armor(ArmorType armorType, int armorHP, boolean metallic,
                 double enragedDamageScale, double enragedSpeedScale, double x, double y) {
        this(armorType, armorHP, metallic, x, y);
        this.enrageable = true;
        this.enragedDamageScale = enragedDamageScale;
        this.enragedSpeedScale = enragedSpeedScale;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (isMagetized) {
            Plant magnet = findTargetMagnet(ctx);
            if (magnet != null) {
                magentized(magnet, 0.05);
            } else {
                destroy();
                isMagetized = false;
            }
        } else {
            this.x = zombie.getX();
            this.y = zombie.getRow();
        }
    }

    @Override
    public void onHit(Zombie zombie, int damage) {
        absorb(damage);
    }

    public int absorb(int damage) {
        armorHP -= damage;
        int overflow = 0;
        if (armorHP < 0) {
            overflow = -armorHP;
            armorHP = 0;
        }
        return overflow;
    }

    public void destroy() {
        armorHP = 0;
    }

    @Override
    public boolean isDestroyed() {
        return armorHP <= 0;
    }

    public void afterDestroy(Zombie zombie) {
        if (enrageable && !enraged) {
            enraged = true;
            zombie.setEatDps(zombie.getEatDps() * enragedDamageScale);
            zombie.setSpeed(zombie.getSpeed() * enragedSpeedScale);
        }
    }

    public void magentized(Plant magnet, double deltaTime) {
        double targetX = magnet.getX();
        double targetY = magnet.getRow();

        double dx = targetX - x;
        double dy = targetY - y;
        double d = Math.hypot(dx, dy);

        if (d < 0.2) {
            isMagetized = false;
            destroy();
            return;
        }

        if (d > 1e-6) {
            double speed = 5.0;
            vx = (dx / d) * speed;
            vy = (dy / d) * speed;

            x += vx * deltaTime;
            y += vy * deltaTime;
        }
    }

    private Plant findTargetMagnet(GameContext ctx) {
        Plant nearest = null;
        double minDistance = Double.MAX_VALUE;

        if (ctx == null || ctx.getPlantGrid() == null) return null;

        for (int r = 0; r < ctx.getPlantGrid().length; r++) {
            for (int c = 0; c < ctx.getPlantGrid()[r].length; c++) {
                Plant p = ctx.getPlantGrid()[r][c];
                if (p != null && "Magnet-shroom".equalsIgnoreCase(p.getName()) && !p.isDead()) {
                    double dist = Math.hypot(p.getX() - x, p.getRow() - y);
                    if (dist < minDistance) {
                        minDistance = dist;
                        nearest = p;
                    }
                }
            }
        }
        return nearest;
    }

    // Getters & Setters
    public int getArmorHP() { return armorHP; }
    public ArmorType getArmorType() { return armorType; }
    public boolean isMetallic() { return metallic; }
    public boolean isEnraged() { return enraged; }
    public double getX() { return x; }
    public double getY() { return y; }
}