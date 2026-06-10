package controller.repository.factory;

import controller.repository.DataManager;
import model.zombie.Zombie;

public class ZombieFactory extends BaseFactory<Zombie> {

    public ZombieFactory(DataManager dm) {
        super(dm);
    }


    @Override
    public Zombie create(String id) {
        //creates new zombie based on csv file
        return null;
    }
}
