package com.workshop.controller.repository.factory;

import com.workshop.controller.repository.DataManager;

public class BaseFactory<T> implements EntityFactory<T> {
    protected final DataManager dataManager;

    public BaseFactory(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public T create(String id) {
        return null;
    }
}
