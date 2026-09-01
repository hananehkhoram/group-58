package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;
import com.workshop.model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class MeleeAttackers implements BaseAbility {

    private static final double FRONT_REACH = 1.55;
    private static final double BACK_REACH = 1.45;
    private static final double CHOMP_REACH = 1.7;
    private static final int INSTANT_KILL = 10000;
    private static final int CHOMPER_FOOD_EATS = 3;
    private static final double CHOMPER_FOOD_REACH = 6.5;

    private long lastActionTick = -1;
    private long digestUntilTick = -1;
    private int heavyStrikesLeft;
    private int heavyStrikeDamage;
    private int ticksUntilNextStrike;

    public void melee(String meleeKind, int damage, Plant plant, GameEngine engine) {
        GameContext ctx = engine.getCtx();
        tickDigest(ctx);
        tickHeavyStrike(plant, ctx);
        if (isDigesting(ctx) || isHeavyStriking()) {
            return;
        }
        if (!cooldownReady(plant, ctx)) {
            return;
        }

        boolean hit = switch (meleeKind == null ? "" : meleeKind) {
            case "FRONT_AND_BACK" -> strikeFrontAndBack(plant, ctx, damage);
            case "AOE" -> strikeAround(plant, ctx, damage, 1);
            case "AOE_RAMP_UP" -> strikeRamp(plant, ctx);
            default -> strikeFront(plant, ctx, damage);
        };

        if (!hit) {
            return;
        }
        ctx.queuePlantAttackAnimation(plant);
        markActed(ctx);
    }

    public void instantEat(Plant plant, GameEngine engine) {
        GameContext ctx = engine.getCtx();
        tickDigest(ctx);
        tickHeavyStrike(plant, ctx);
        if (isDigesting(ctx) || isHeavyStriking()) {
            return;
        }

        Zombie prey = nearestPrey(plant, ctx, CHOMP_REACH);
        if (prey == null || prey.isBoss()) {
            return;
        }

        prey.takeDamage(INSTANT_KILL);
        ctx.queuePlantAttackAnimation(plant);
        markActed(ctx);
        double digestSeconds = plant.getActionInterval() != null ? plant.getActionInterval() : 40.0;
        digestUntilTick = ctx.getTimeManager().getTotalTicks()
            + Math.max(10L, Math.round(digestSeconds * 10.0));
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        if (self == null || ctx == null || mode == null) {
            return;
        }
        switch (mode) {
            case HEAVY_STRIKE -> startHeavyStrike(self, ctx);
            case MULTI_TARGET_BURST -> chompDistantZombies(self, ctx);
            default -> {
            }
        }
    }

    private void startHeavyStrike(Plant self, GameContext ctx) {
        heavyStrikeDamage = Math.max(20, parseStageDamage(self.getDamage(), 0) * 2);
        heavyStrikesLeft = 8;
        ticksUntilNextStrike = 0;
        self.setPlantFoodActive(true);
        self.startPlantFoodGlow(1.8f);
        ctx.queuePlantAttackAnimation(self);
        tickHeavyStrike(self, ctx);
    }

    private void tickHeavyStrike(Plant plant, GameContext ctx) {
        if (!isHeavyStriking()) {
            return;
        }
        if (ticksUntilNextStrike > 0) {
            ticksUntilNextStrike--;
            return;
        }
        strikeAround(plant, ctx, heavyStrikeDamage, 1);
        ctx.queuePlantAttackAnimation(plant);
        heavyStrikesLeft--;
        ticksUntilNextStrike = 2;
        if (!isHeavyStriking()) {
            plant.setPlantFoodActive(false);
        }
    }

    private void chompDistantZombies(Plant plant, GameContext ctx) {
        int eaten = 0;
        List<Zombie> prey = new ArrayList<>(ctx.getAliveZombies());
        prey.sort((a, b) -> Double.compare(distance(plant, a), distance(plant, b)));
        for (Zombie zombie : prey) {
            if (zombie == null || zombie.isDead() || zombie.isBoss()) {
                continue;
            }
            if (distance(plant, zombie) > CHOMPER_FOOD_REACH) {
                continue;
            }
            zombie.takeDamage(INSTANT_KILL);
            eaten++;
            if (eaten >= CHOMPER_FOOD_EATS) {
                break;
            }
        }
        if (eaten > 0) {
            ctx.queuePlantAttackAnimation(plant);
            plant.startPlantFoodGlow(1.6f);
        }
    }

    private boolean strikeFront(Plant plant, GameContext ctx, int damage) {
        Zombie target = nearestPrey(plant, ctx, FRONT_REACH);
        if (target == null) {
            return false;
        }
        target.takeDamage(damage);
        return true;
    }

    private boolean strikeFrontAndBack(Plant plant, GameContext ctx, int damage) {
        boolean hit = false;
        for (Zombie zombie : ctx.getAliveZombies()) {
            if (!isInFrontOrBack(plant, zombie)) {
                continue;
            }
            zombie.takeDamage(damage);
            hit = true;
        }
        return hit;
    }

    private boolean strikeAround(Plant plant, GameContext ctx, int damage, int radius) {
        boolean hit = false;
        for (Zombie zombie : ctx.getAliveZombies()) {
            if (zombie == null || zombie.isDead()) {
                continue;
            }
            if (Math.abs(zombie.getY() - plant.getRow()) > radius) {
                continue;
            }
            if (Math.abs(zombie.getX() - plant.getCol()) > radius + 0.45) {
                continue;
            }
            zombie.takeDamage(damage);
            hit = true;
        }
        return hit;
    }

    private boolean strikeRamp(Plant plant, GameContext ctx) {
        int stage = kiwibeastStage(plant, ctx);
        int damage = parseStageDamage(plant.getDamage(), stage);
        return strikeAround(plant, ctx, damage, 1 + stage);
    }

    private Zombie nearestPrey(Plant plant, GameContext ctx, double reach) {
        Zombie best = null;
        double bestX = Double.MAX_VALUE;
        for (Zombie zombie : ctx.getAliveZombies()) {
            if (zombie == null || zombie.isDead() || zombie.isBoss()) {
                continue;
            }
            if (!zombie.occupiesRow(plant.getRow())) {
                continue;
            }
            double x = zombie.getX();
            if (x < plant.getCol() - 0.2 || x > plant.getCol() + reach) {
                continue;
            }
            if (x < bestX) {
                bestX = x;
                best = zombie;
            }
        }
        return best;
    }

    private boolean isInFrontOrBack(Plant plant, Zombie zombie) {
        if (zombie == null || zombie.isDead()) {
            return false;
        }
        if (!zombie.occupiesRow(plant.getRow())) {
            return false;
        }
        double dx = zombie.getX() - plant.getCol();
        return (dx >= -0.2 && dx <= FRONT_REACH)
            || (dx < 0 && dx >= -BACK_REACH);
    }

    private boolean cooldownReady(Plant plant, GameContext ctx) {
        long now = ctx.getTimeManager().getTotalTicks();
        double interval = plant.getActionInterval() != null ? plant.getActionInterval() : 1.0;
        long needed = Math.max(1L, Math.round(interval * 10.0));
        return lastActionTick < 0 || now - lastActionTick >= needed;
    }

    private void markActed(GameContext ctx) {
        lastActionTick = ctx.getTimeManager().getTotalTicks();
    }

    private void tickDigest(GameContext ctx) {
        if (digestUntilTick < 0) {
            return;
        }
        if (ctx.getTimeManager().getTotalTicks() >= digestUntilTick) {
            digestUntilTick = -1;
        }
    }

    private boolean isDigesting(GameContext ctx) {
        return digestUntilTick >= 0 && ctx.getTimeManager().getTotalTicks() < digestUntilTick;
    }

    private boolean isHeavyStriking() {
        return heavyStrikesLeft > 0;
    }

    private int kiwibeastStage(Plant plant, GameContext ctx) {
        int age = ctx.getTimeManager().getTotalSeconds() - plant.getPlantTimeSecond();
        if (age >= 72) {
            return 2;
        }
        if (age >= 24) {
            return 1;
        }
        return 0;
    }

    private static int parseStageDamage(String raw, int stage) {
        if (raw == null || raw.isBlank()) {
            return 15;
        }
        raw = raw.trim();
        if (raw.contains("/")) {
            String[] parts = raw.split("/");
            int idx = Math.max(0, Math.min(stage, parts.length - 1));
            try {
                return Integer.parseInt(parts[idx].trim());
            } catch (NumberFormatException ignored) {
                return 15;
            }
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return 15;
        }
    }

    private static double distance(Plant plant, Zombie zombie) {
        double dRow = zombie.getY() - plant.getRow();
        double dCol = zombie.getX() - plant.getCol();
        return Math.hypot(dRow, dCol);
    }
}
