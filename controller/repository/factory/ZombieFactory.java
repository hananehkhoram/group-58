package controller.repository.factory;

import controller.repository.DataManager;
import model.zombie.Zombie;

public class ZombieFactory extends BaseFactory<Zombie> {

    public ZombieFactory(DataManager dm) {
        super(dm);
    }

    @Override
    public Zombie create(String name) {
        Zombie z = dataManager.zombies.get(name);
        if (z == null) {
            throw new IllegalArgumentException("Zombie template not found in repository: " + name);
        }
        Zombie newZombie = new Zombie(z.getId(), z.getName(), z.getHp(), z.getEatDps(), z.getSpeed(),
                z.getWavePointCost(), 0);

        return z;
    }
}
