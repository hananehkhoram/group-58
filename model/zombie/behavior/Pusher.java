package model.zombie.behavior;

import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.plants.Plant;
import model.zombie.Effects;
import model.zombie.Zombie;
import view.ConsoleView;

public class Pusher implements Behaviors {

    public enum PushType {
        ARCADE,
        BARREL_ROLLER,
        TROGLOBITE
    }

    private PushType pushType;
    private boolean pushesObject = true;
    private double objectHealth;

    public Pusher(PushType pushType) {
        this.pushType = pushType;
        if (pushType == PushType.ARCADE) {
            this.objectHealth = 1200;
        } else if (pushType == PushType.BARREL_ROLLER) {
            this.objectHealth = 400;
        } else {
            this.objectHealth = 800;
        }
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (!pushesObject) return;

        int row = zombie.getRow();
        int col = (int) zombie.getX();
        int totalCols = ctx.getPlantGrid()[0].length;

        if (col < 0 || col >= totalCols) return;

        Plant targetPlant = ctx.getPlantGrid()[row][col];
        if (targetPlant != null && !targetPlant.isDead()) {
            targetPlant.takeDamage(Integer.MAX_VALUE);
            ConsoleView.showMessage(zombie.getName() + " crushed plant " + targetPlant.getName() + "!");
        }

        for (Zombie otherZombie : ctx.getAliveZombies()) {
            if (otherZombie == zombie || otherZombie.getRow() != row) continue;

            if (otherZombie.searchEffect(Effects.HYPNOTIZED) && Math.abs(otherZombie.getX() - zombie.getX()) < 0.8) {
                otherZombie.takeDamage(Integer.MAX_VALUE);
                ConsoleView.showMessage(zombie.getName() + " crushed hypnotized zombie!");
            }
        }
    }


    public void takeDamageToObject(Zombie zombie, int damage, GameContext ctx) {
        if (!pushesObject) return;

        this.objectHealth -= damage;
        if (this.objectHealth <= 0) {
            destroyPushedObject(zombie, ctx);
        }
    }

    public void destroyPushedObject(Zombie zombie, GameContext ctx) {
        this.pushesObject = false;

        if (pushType == PushType.BARREL_ROLLER) {
            ConsoleView.showMessage("Barrel destroyed! Imps popping out!");
            spawnBarrelImps(zombie, ctx);
        } else if (pushType == PushType.TROGLOBITE) {
            ConsoleView.showMessage("Ice blocks broken!");
        } else if (pushType == PushType.ARCADE) {
            ConsoleView.showMessage("Arcade machine destroyed!");
        }
    }

    private void spawnBarrelImps(Zombie zombie, GameContext ctx) {
        ZombieFactory zombieFactory = new ZombieFactory(ctx.getDataManager());

        for (int i = 0; i < 2; i++) {
            Zombie imp = zombieFactory.create("Imp");
            if (imp != null) {
                imp.setRow(zombie.getRow());
                imp.setY(zombie.getRow());
                imp.setX(zombie.getX());
                ctx.addZombie(imp);
            }
        }
    }

}