package com.workshop.controller;

import com.workshop.model.GameContext;
import com.workshop.model.Scoring.ScoringPattern;
import com.workshop.model.user.User;
import com.workshop.model.zombie.Zombie;

import java.util.List;


public class ScoringManager {

    private static final long QUICK_KILL_TICK_THRESHOLD = 100;

    private static final int PRECISION_OVERKILL_MARGIN = 5;


    public static void onZombiesDied(GameContext ctx, List<Zombie> deathsThisTick) {
        if (deathsThisTick == null || deathsThisTick.isEmpty()) return;

        if (deathsThisTick.size() >= 2) {
            ctx.incrementSimultaneousKillPattern();
            ctx.announce("Simultaneous Kill: " +ScoringPattern.SIMULTANEOUS_KILL.getPoints()+ "meow points!");

        }

        long now = ctx.getTimeManager().getTotalTicks();
        for (Zombie z : deathsThisTick) {
            if (now - z.getSpawnTick() <= QUICK_KILL_TICK_THRESHOLD) {
                ctx.incrementQuickKillPattern();
                ctx.announce("Quick Kill: "+ ScoringPattern.QUICK_KILL.getPoints()+ "meow points!");
            }

            int overkill = -z.getHp();
            if (overkill <= PRECISION_OVERKILL_MARGIN) {
                ctx.incrementPrecisionFinishPattern();
                ctx.announce("Precision Finish: " +ScoringPattern.PRECISION_FINISH.getPoints() + "meow points!");
            }

            ctx.bumpKillStreak();
            ctx.announce("Kill Streak: " + ScoringPattern.MULTI_KILL.getPoints()+ "meow points!");
        }
    }


    public static void onProjectileKill(GameContext ctx, int killCountForThisProjectile) {
        if (killCountForThisProjectile >= 2) {
            ctx.incrementMultiKillPattern();
            ctx.announce("Multi Kill: " +ScoringPattern.MULTI_KILL.getPoints()+ "meow points!");

        }
    }


    public static void evaluateLevelEndScoring(GameContext ctx, User user) {
        int total = ctx.getMultiKillPatternCount() * ScoringPattern.MULTI_KILL.getPoints()
                + ctx.getSimultaneousKillPatternCount() * ScoringPattern.SIMULTANEOUS_KILL.getPoints()
                + ctx.getQuickKillPatternCount() * ScoringPattern.QUICK_KILL.getPoints()
                + ctx.getKillStreakPatternCount() * ScoringPattern.KILL_STREAK.getPoints()
                + ctx.getPrecisionFinishPatternCount() * ScoringPattern.PRECISION_FINISH.getPoints();

        StringBuilder sb = new StringBuilder("=== MewPoint Scoring ===\n");
        appendLine(sb, ScoringPattern.MULTI_KILL, ctx.getMultiKillPatternCount());
        appendLine(sb, ScoringPattern.SIMULTANEOUS_KILL, ctx.getSimultaneousKillPatternCount());
        appendLine(sb, ScoringPattern.QUICK_KILL, ctx.getQuickKillPatternCount());
        appendLine(sb, ScoringPattern.KILL_STREAK, ctx.getKillStreakPatternCount());
        appendLine(sb, ScoringPattern.PRECISION_FINISH, ctx.getPrecisionFinishPatternCount());
        sb.append("Total MewPoint earned: ").append(total).append("\n");

        boolean isNewRecord = total > user.getMaxMewPoint();
        if (isNewRecord) {
            user.setMaxMewPoint(total);
            sb.append("New personal best! Max MewPoint: ").append(total);
        } else {
            sb.append("Personal best remains: ").append(user.getMaxMewPoint());
        }

        com.workshop.net.GameClient client = com.workshop.net.GameClient.get();
        if (client.isConnected()) {
            com.workshop.net.NetResponse submitted = client.submitBonusScore(total);
            if (submitted.ok) {
                user.setHasNetworkBonusScore(true);
                if (submitted.payload != null && !submitted.payload.isBlank()) {
                    com.workshop.net.UserSnapshot.fromWire(submitted.payload).applyTo(user);
                }
            }
        }

        com.workshop.view.Console.showMessage(sb.toString());
    }

    private static void appendLine(StringBuilder sb, ScoringPattern pattern, int count) {
        if (count <= 0) return;
        sb.append(pattern.getTitle()).append(" x").append(count)
                .append(" = ").append(count * pattern.getPoints()).append(" pts (")
                .append(pattern.getDescription()).append(")\n");
    }
}
