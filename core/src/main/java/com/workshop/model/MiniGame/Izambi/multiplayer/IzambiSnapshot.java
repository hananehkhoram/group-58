package com.workshop.model.MiniGame.Izambi.multiplayer;

import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;
import com.workshop.net.UserSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight, render-only snapshot of one tick of a 2-player "I, Zombie"
 * match. The host builds one of these from its live {@link GameContext}
 * after every simulation step and sends it to the guest.
 * <p>
 * Each entity carries a stable {@code id} (the host's
 * {@link System#identityHashCode} for that Plant/Zombie, stable for the
 * object's whole lifetime in this match) so the guest can keep reusing the
 * same mirrored Plant/Zombie objects across snapshots instead of recreating
 * them every tick — recreating them would make the real PAM-based rendering
 * layers (which cache one actor per object identity) think every unit is a
 * brand-new spawn on every update, breaking smooth animation.
 * <p>
 * "Sun Producer Zombie" units are deliberately left out: they're an
 * internal bookkeeping trick (a renamed, immobile zombie template) with no
 * real animation of their own, and the single-player screen doesn't show
 * them either.
 */
public final class IzambiSnapshot {

    private static final String SUN_PRODUCER_NAME = "Sun Producer Zombie";

    public static final class EntityView {
        public final int id;
        public final int row;
        public final double x;
        public final String name;
        public final int hpPercent;
        public final boolean eating;

        public EntityView(int id, int row, double x, String name, int hpPercent, boolean eating) {
            this.id = id;
            this.row = row;
            this.x = x;
            this.name = name;
            this.hpPercent = hpPercent;
            this.eating = eating;
        }
    }

    public int zombieSun;
    public int plantSun;
    public int remainingSeconds;
    public boolean ended;
    public MatchRole winner;
    public boolean[] brainsEaten;
    public List<EntityView> plants = new ArrayList<>();
    public List<EntityView> zombies = new ArrayList<>();

    public static IzambiSnapshot capture(
        GameContext ctx,
        IZombieManager iZombieManager,
        int plantSun,
        int remainingSeconds,
        boolean ended,
        MatchRole winner
    ) {
        IzambiSnapshot snap = new IzambiSnapshot();
        snap.zombieSun = ctx.getSunAmount();
        snap.plantSun = plantSun;
        snap.remainingSeconds = Math.max(0, remainingSeconds);
        snap.ended = ended;
        snap.winner = winner;

        int rows = ctx.getLevel().getRows();
        snap.brainsEaten = new boolean[rows];
        for (int row = 0; row < rows; row++) {
            snap.brainsEaten[row] = iZombieManager.isBrainEaten(row);
        }

        for (Plant plant : ctx.getAlivePlants()) {
            if (plant == null || plant.isDead()) {
                continue;
            }
            int hpPercent = plant.getBaseHp() <= 0
                ? 100
                : (int) Math.round(100.0 * plant.getHp() / plant.getBaseHp());
            snap.plants.add(new EntityView(
                System.identityHashCode(plant), plant.getRow(), plant.getCol(), plant.getName(), hpPercent, false
            ));
        }

        for (Zombie zombie : ctx.getAliveZombies()) {
            if (zombie == null || zombie.isDead() || SUN_PRODUCER_NAME.equals(zombie.getName())) {
                continue;
            }
            int hpPercent = zombie.getMaxHp() <= 0
                ? 100
                : (int) Math.round(100.0 * zombie.getHp() / zombie.getMaxHp());
            snap.zombies.add(new EntityView(
                System.identityHashCode(zombie), zombie.getRow(), zombie.getX(), zombie.getName(),
                hpPercent, zombie.isEating()
            ));
        }

        return snap;
    }

    public String toWire() {
        StringBuilder brains = new StringBuilder();
        if (brainsEaten != null) {
            for (boolean b : brainsEaten) {
                brains.append(b ? '1' : '0');
            }
        }
        return UserSnapshot.join(
            String.valueOf(zombieSun),
            String.valueOf(plantSun),
            String.valueOf(remainingSeconds),
            ended ? "1" : "0",
            winner == null ? "" : winner.name(),
            brains.toString(),
            entitiesToWire(plants),
            entitiesToWire(zombies)
        );
    }

    private static String entitiesToWire(List<EntityView> entities) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entities.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            EntityView e = entities.get(i);
            sb.append(e.id).append(',').append(e.row).append(',').append(e.x)
                .append(',').append(e.name).append(',').append(e.hpPercent)
                .append(',').append(e.eating ? '1' : '0');
        }
        return sb.toString();
    }

    public static IzambiSnapshot fromWire(String wire) {
        String[] p = UserSnapshot.split(wire);
        IzambiSnapshot snap = new IzambiSnapshot();
        snap.zombieSun = Integer.parseInt(p[0]);
        snap.plantSun = Integer.parseInt(p[1]);
        snap.remainingSeconds = Integer.parseInt(p[2]);
        snap.ended = "1".equals(p[3]);
        snap.winner = p[4].isEmpty() ? null : MatchRole.fromWire(p[4]);

        String brainsStr = p[5];
        snap.brainsEaten = new boolean[brainsStr.length()];
        for (int i = 0; i < brainsStr.length(); i++) {
            snap.brainsEaten[i] = brainsStr.charAt(i) == '1';
        }

        snap.plants = entitiesFromWire(p[6]);
        snap.zombies = entitiesFromWire(p[7]);
        return snap;
    }

    private static List<EntityView> entitiesFromWire(String wire) {
        List<EntityView> result = new ArrayList<>();
        if (wire == null || wire.isBlank()) {
            return result;
        }
        for (String entry : wire.split(";", -1)) {
            if (entry.isBlank()) {
                continue;
            }
            String[] fields = entry.split(",", -1);
            int id = Integer.parseInt(fields[0]);
            int row = Integer.parseInt(fields[1]);
            double x = Double.parseDouble(fields[2]);
            String name = fields[3];
            int hpPercent = Integer.parseInt(fields[4]);
            boolean eating = fields.length > 5 && "1".equals(fields[5]);
            result.add(new EntityView(id, row, x, name, hpPercent, eating));
        }
        return result;
    }
}
