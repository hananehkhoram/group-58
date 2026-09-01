package com.workshop.model.MiniGame.Izambi.multiplayer;

import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.zombie.Zombie;
import com.workshop.net.UserSnapshot;

import java.util.ArrayList;
import java.util.List;

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

    public static final class ZombieView {
        public final int id;
        public final int row;
        public final double x;
        public final String name;
        public final int hpPercent;
        public final boolean eating;
        public final boolean ashed;
        public final boolean ashFinished;
        public final boolean deathAnimFinished;
        public final boolean iced;
        public final boolean initialFrozenBlock;
        public final double iceHp;
        public final int armorHp;

        public ZombieView(
            int id, int row, double x, String name, int hpPercent, boolean eating,
            boolean ashed, boolean ashFinished, boolean deathAnimFinished,
            boolean iced, boolean initialFrozenBlock, double iceHp, int armorHp
        ) {
            this.id = id;
            this.row = row;
            this.x = x;
            this.name = name;
            this.hpPercent = hpPercent;
            this.eating = eating;
            this.ashed = ashed;
            this.ashFinished = ashFinished;
            this.deathAnimFinished = deathAnimFinished;
            this.iced = iced;
            this.initialFrozenBlock = initialFrozenBlock;
            this.iceHp = iceHp;
            this.armorHp = armorHp;
        }
    }

    public static final class ProjectileView {
        public final int id;
        public final int row;
        public final double x;
        public final double y;
        public final String bulletType;
        public final String trajectory;
        public final boolean isFromZombie;
        public final int ownerPlantId;

        public ProjectileView(
            int id,
            int row,
            double x,
            double y,
            String bulletType,
            String trajectory,
            boolean isFromZombie,
            int ownerPlantId
        ) {
            this.id = id;
            this.row = row;
            this.x = x;
            this.y = y;
            this.bulletType = bulletType;
            this.trajectory = trajectory;
            this.isFromZombie = isFromZombie;
            this.ownerPlantId = ownerPlantId;
        }
    }

    public int zombieSun;
    public int plantSun;
    public int remainingSeconds;
    public boolean ended;
    public MatchRole winner;
    public boolean[] brainsEaten;
    public List<EntityView> plants = new ArrayList<>();
    public List<ZombieView> zombies = new ArrayList<>();
    public List<ProjectileView> projectiles = new ArrayList<>();

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
            if (zombie == null || SUN_PRODUCER_NAME.equals(zombie.getName())) {
                continue;
            }

            int hpPercent = zombie.getMaxHp() <= 0
                ? 100
                : (int) Math.round(100.0 * zombie.getHp() / zombie.getMaxHp());
            hpPercent = Math.max(0, hpPercent);

            int armorHp = zombie.getArmor() != null ? zombie.getArmor().getArmorHP() : -1;

            snap.zombies.add(new ZombieView(
                System.identityHashCode(zombie),
                zombie.getRow(),
                zombie.getX(),
                zombie.getName(),
                hpPercent,
                zombie.isEating(),
                zombie.isAshed(),
                zombie.isAshFinished(),
                zombie.isDeathAnimFinished(),
                zombie.isIced(),
                zombie.isInitialFrozenBlock(),
                zombie.getIceHp(),
                armorHp
            ));
        }

        for (Projectile projectile : ctx.getProjectiles()) {
            if (projectile == null || !projectile.isActive()) {
                continue;
            }
            int ownerPlantId = projectile.getOwnerPlant() != null
                ? System.identityHashCode(projectile.getOwnerPlant())
                : -1;
            snap.projectiles.add(new ProjectileView(
                System.identityHashCode(projectile),
                projectile.getRow(),
                projectile.getX(),
                projectile.getY(),
                projectile.getBulletType().name(),
                projectile.getTrajectory().name(),
                projectile.isFromZombie(),
                ownerPlantId
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
            zombiesToWire(zombies),
            projectilesToWire(projectiles)
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

    private static String zombiesToWire(List<ZombieView> zombies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < zombies.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            ZombieView z = zombies.get(i);
            sb.append(z.id).append(',').append(z.row).append(',').append(z.x)
                .append(',').append(z.name).append(',').append(z.hpPercent)
                .append(',').append(z.eating ? '1' : '0')
                .append(',').append(z.ashed ? '1' : '0')
                .append(',').append(z.ashFinished ? '1' : '0')
                .append(',').append(z.deathAnimFinished ? '1' : '0')
                .append(',').append(z.iced ? '1' : '0')
                .append(',').append(z.initialFrozenBlock ? '1' : '0')
                .append(',').append(z.iceHp)
                .append(',').append(z.armorHp);
        }
        return sb.toString();
    }

    private static String projectilesToWire(List<ProjectileView> entities) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entities.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            ProjectileView p = entities.get(i);
            sb.append(p.id).append(',').append(p.row).append(',').append(p.x)
                .append(',').append(p.y).append(',').append(p.bulletType)
                .append(',').append(p.trajectory).append(',').append(p.isFromZombie ? '1' : '0')
                .append(',').append(p.ownerPlantId);
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
        snap.zombies = zombiesFromWire(p[7]);
        snap.projectiles = p.length > 8 ? projectilesFromWire(p[8]) : new ArrayList<>();
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

    private static List<ZombieView> zombiesFromWire(String wire) {
        List<ZombieView> result = new ArrayList<>();
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
            boolean eating = "1".equals(fields[5]);
            boolean ashed = "1".equals(fields[6]);
            boolean ashFinished = "1".equals(fields[7]);
            boolean deathAnimFinished = "1".equals(fields[8]);
            boolean iced = "1".equals(fields[9]);
            boolean initialFrozenBlock = "1".equals(fields[10]);
            double iceHp = Double.parseDouble(fields[11]);
            int armorHp = Integer.parseInt(fields[12]);
            result.add(new ZombieView(
                id, row, x, name, hpPercent, eating,
                ashed, ashFinished, deathAnimFinished,
                iced, initialFrozenBlock, iceHp, armorHp
            ));
        }
        return result;
    }

    private static List<ProjectileView> projectilesFromWire(String wire) {
        List<ProjectileView> result = new ArrayList<>();
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
            double y = Double.parseDouble(fields[3]);
            String bulletType = fields[4];
            String trajectory = fields[5];
            boolean isFromZombie = "1".equals(fields[6]);
            int ownerPlantId = Integer.parseInt(fields[7]);
            result.add(new ProjectileView(id, row, x, y, bulletType, trajectory, isFromZombie, ownerPlantId));
        }
        return result;
    }
}
