package controller.repository.factory;

import controller.repository.DataManager;
import model.user.User;
import model.zombie.Zombie;

public class UserFactory extends BaseFactory<User> {

    public UserFactory(DataManager dm) {
        super(dm);
    }


    @Override
    public User create(String id) {
        //creates new user based on csv file
        return null;
    }
}
