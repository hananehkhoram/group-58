package com.workshop.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {
    private final ConcurrentHashMap<String, ClientHandler> online = new ConcurrentHashMap<>();

    public void bind(String username, ClientHandler handler) {
        online.put(username.toLowerCase(), handler);
    }

    public void unbind(ClientHandler handler) {
        online.entrySet().removeIf(entry -> entry.getValue() == handler);
    }

    public boolean isOnline(String username) {
        return online.containsKey(username.toLowerCase());
    }

    public ClientHandler handlerFor(String username) {
        return online.get(username.toLowerCase());
    }

    public List<String> onlineUsernames() {
        return new ArrayList<>(online.keySet());
    }
}
