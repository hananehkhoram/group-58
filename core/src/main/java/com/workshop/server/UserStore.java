package com.workshop.server;

import com.workshop.net.UserSnapshot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UserStore {
    private final Path file;
    private final Map<String, UserSnapshot> users = new LinkedHashMap<>();

    public UserStore(Path file) {
        this.file = file;
        load();
    }

    public synchronized UserSnapshot find(String username) {
        for (UserSnapshot snap : users.values()) {
            if (snap.username.equalsIgnoreCase(username)) {
                return snap;
            }
        }
        return null;
    }

    public synchronized boolean exists(String username) {
        return find(username) != null;
    }

    public synchronized void put(UserSnapshot snap) {
        users.put(snap.username.toLowerCase(), snap);
        save();
    }

    public synchronized List<UserSnapshot> all() {
        return new ArrayList<>(users.values());
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    UserSnapshot snap = UserSnapshot.fromWire(line);
                    users.put(snap.username.toLowerCase(), snap);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load server users", e);
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            for (UserSnapshot snap : users.values()) {
                lines.add(snap.toWire());
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not save server users", e);
        }
    }

    public synchronized UserSnapshot rename(
        String oldUsername,
        String newUsername
    ) {
        UserSnapshot snap = users.remove(oldUsername.toLowerCase());

        if (snap == null) {
            return null;
        }

        snap.username = newUsername;
        users.put(newUsername.toLowerCase(), snap);
        save();

        return snap;
    }
}
