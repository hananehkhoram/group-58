package com.workshop.controller.repository.factory;

public interface EntityFactory<T> {
    T create(String id);
}
