package com.workshop.server;

import com.workshop.model.user.Security;
import com.workshop.net.UserSnapshot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class ClientHandler implements Runnable {
    private final Socket socket;
    private final UserStore store;
    private final SessionManager sessions;
    private String username;

    public ClientHandler(Socket socket, UserStore store, SessionManager sessions) {
        this.socket = socket;
        this.store = store;
        this.sessions = sessions;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                out.println(handle(line));
            }
        } catch (Exception ignored) {
        } finally {
            sessions.unbind(this);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private String handle(String line) {
        String[] p = UserSnapshot.split(line);
        String type = p[0];
        try {
            return switch (type) {
                case "PING" -> ok("");
                case "REGISTER" -> register(p);
                case "LOGIN" -> login(p);
                case "LOGOUT" -> logout();
                case "SYNC_PROFILE" -> syncProfile(p);
                case "UPDATE_NICKNAME" -> updateNickname(p);
                case "GET_LEADERBOARD" -> leaderboard();
                case "SUBMIT_BONUS_SCORE" -> submitBonus(p);
                case "GET_ONLINE_USERS" -> ok(String.join(",", sessions.onlineUsernames()));
                default -> err("Unknown message type: " + type);
            };
        } catch (Exception e) {
            return err(e.getMessage() == null ? "Server error." : e.getMessage());
        }
    }

    private String register(String[] p) {
        String username = p[1];
        String password = p[2];
        if (store.exists(username)) {
            return err("Username already exists.");
        }
        UserSnapshot snap = new UserSnapshot();
        snap.username = username;
        snap.passwordHash = Security.hashPassword(password);
        snap.nickName = p[3];
        snap.email = p[4];
        snap.gender = p[5];
        snap.coins = 2000;
        snap.gems = 5;
        store.put(snap);
        return ok(snap.toWire());
    }

    private String login(String[] p) {
        UserSnapshot snap = store.find(p[1]);
        if (snap == null) {
            return err("User does not exist!");
        }
        String incoming = p[2];
        boolean passwordOk = snap.passwordHash.equals(incoming)
            || snap.passwordHash.equals(Security.hashPassword(incoming));
        if (!passwordOk) {
            return err("Incorrect password.");
        }
        username = snap.username;
        sessions.bind(username, this);
        return ok(snap.toWire());
    }

    private String logout() {
        sessions.unbind(this);
        username = null;
        return ok("");
    }

    private String syncProfile(String[] p) {
        if (username == null) {
            return err("Not logged in.");
        }
        UserSnapshot snap = store.find(username);
        snap.coins = Integer.parseInt(p[1]);
        snap.gems = Integer.parseInt(p[2]);
        snap.lastLevel = Integer.parseInt(p[3]);
        snap.lastSeason = Integer.parseInt(p[4]);
        snap.minigamesCompleted = Integer.parseInt(p[5]);
        snap.dailyQuests = Integer.parseInt(p[6]);
        snap.otherQuests = Integer.parseInt(p[7]);
        snap.maxMewPoint = Integer.parseInt(p[8]);
        store.put(snap);
        return ok(snap.toWire());
    }

    private String updateNickname(String[] p) {
        if (username == null) {
            return err("Not logged in.");
        }
        String nickName = p.length > 1 ? p[1].trim() : "";
        if (!isNickNameValid(nickName)) {
            return err("Invalid nickname.");
        }
        UserSnapshot snap = store.find(username);
        if (snap == null) {
            return err("User does not exist!");
        }
        snap.nickName = nickName;
        store.put(snap);
        return ok(snap.toWire());
    }

    private static boolean isNickNameValid(String nickName) {
        return nickName != null
            && nickName.length() >= 3
            && nickName.length() <= 30;
    }

    private String submitBonus(String[] p) {
        if (username == null) {
            return err("Not logged in.");
        }
        int score = Integer.parseInt(p[1]);
        UserSnapshot snap = store.find(username);
        snap.hasNetworkBonusScore = true;
        if (score > snap.maxMewPoint) {
            snap.maxMewPoint = score;
        }
        store.put(snap);
        return ok(snap.toWire());
    }

    private String leaderboard() {
        StringBuilder sb = new StringBuilder();
        for (UserSnapshot snap : store.all()) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(snap.toWire());
        }
        return ok(sb.toString());
    }

    private static String ok(String payload) {
        return UserSnapshot.join("OK", payload == null ? "" : payload);
    }

    private static String err(String message) {
        return UserSnapshot.join("ERR", message);
    }
}
