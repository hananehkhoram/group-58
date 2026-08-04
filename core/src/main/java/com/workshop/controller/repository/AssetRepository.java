package com.workshop.controller.repository;

public interface AssetRepository<T> {
    void load(String filePath);

    T get(String id);
}
