package controller.repository.factory;

import controller.repository.DataManager;

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
