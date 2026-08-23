package com.workshop.controller.repository.factory;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.user.UserManager;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.ZombieActivator;

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
        int dl = UserManager.getInstance().getCurrentUser().getDifficultyLevel();
        double increaseFactor = dl / 3.0;

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

        newZombie.setHp((int) (newZombie.getHp() * increaseFactor));
        newZombie.setMaxHp(newZombie.getHp());
        newZombie.setEatDps(newZombie.getEatDps() * increaseFactor);
        newZombie.setSpeed(newZombie.getSpeed() * increaseFactor);

        return newZombie;
    }
}
