package com.workshop.controller.repository.factory;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.user.User;

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
