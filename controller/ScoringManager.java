package controller;

import model.GameContext;
import model.Scoring.ScoringPattern;
import model.user.User;
import model.zombie.Zombie;

import java.util.List;


public class ScoringManager {

    private static final long QUICK_KILL_TICK_THRESHOLD = 30; // 3 ثانیه

    private static final int PRECISION_OVERKILL_MARGIN = 5;


    public static void onZombiesDied(GameContext ctx, List<Zombie> deathsThisTick) {
        if (deathsThisTick == null || deathsThisTick.isEmpty()) return;

        if (deathsThisTick.size() >= 2) {
            ctx.incrementSimultaneousKillPattern();
        }

        long now = ctx.getTimeManager().getTotalTicks();
        for (Zombie z : deathsThisTick) {
            if (now - z.getSpawnTick() <= QUICK_KILL_TICK_THRESHOLD) {
                ctx.incrementQuickKillPattern();
            }

            int overkill = -z.getHp(); // hp زیر صفر رفته؛ هرچی منفی‌تر یعنی آسیب بیشتر هدر رفته
            if (overkill <= PRECISION_OVERKILL_MARGIN) {
                ctx.incrementPrecisionFinishPattern();
            }

            ctx.bumpKillStreak();
        }
    }


    public static void onProjectileKill(GameContext ctx, int killCountForThisProjectile) {
        if (killCountForThisProjectile == 2) {
            ctx.incrementMultiKillPattern();
        }
    }


    public static void evaluateLevelEndScoring(GameContext ctx, User user) {
        int total = ctx.getMultiKillPatternCount() * ScoringPattern.MULTI_KILL.getPoints()
                + ctx.getSimultaneousKillPatternCount() * ScoringPattern.SIMULTANEOUS_KILL.getPoints()
                + ctx.getQuickKillPatternCount() * ScoringPattern.QUICK_KILL.getPoints()
                + ctx.getKillStreakPatternCount() * ScoringPattern.KILL_STREAK.getPoints()
                + ctx.getPrecisionFinishPatternCount() * ScoringPattern.PRECISION_FINISH.getPoints();

        if (total > user.getMaxMewPoint()) {
            user.setMaxMewPoint(total);
        }
    }
}