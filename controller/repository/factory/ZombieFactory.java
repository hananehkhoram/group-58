package controller.repository.factory;

import controller.repository.DataManager;
import model.zombie.Zombie;
import model.zombie.ZombieActivator;

public class ZombieFactory extends BaseFactory<Zombie> {

    public ZombieFactory(DataManager dm) {
        super(dm);
    }

    @Override
    public Zombie create(String name) {
        Zombie template = dataManager.zombies.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Zombie template not found in repository: " + name);
        }

        Zombie newZombie = new Zombie();
        newZombie.setId(template.getId());
        newZombie.setName(template.getName());
        newZombie.setHp(template.getHp());
        newZombie.setEatDps(template.getEatDps());
        newZombie.setSpeed(template.getSpeed());
        newZombie.setWavePointCost(template.getWavePointCost());
        newZombie.setWeight(template.getWeight());
        newZombie.setExtraParams(template.getExtraParams());

        newZombie.setBehaviors(ZombieActivator.buildBehaviors(newZombie));

        return newZombie;
    }
}