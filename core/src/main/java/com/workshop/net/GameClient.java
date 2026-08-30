package com.workshop.net;

import com.workshop.model.user.Gender;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class GameClient {
    public static final int DEFAULT_PORT = 5454;
    public static final String DEFAULT_HOST = "127.0.0.1";

    private static final GameClient INSTANCE = new GameClient();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String loggedUsername;

    public static GameClient get() {
        return INSTANCE;
    }

    public synchronized boolean connect(String host, int port) {
        disconnect();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 1500);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            NetResponse ping = request("PING");
            return ping.ok;
        } catch (Exception e) {
            disconnect();
            return false;
        }
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void disconnect() {
        loggedUsername = null;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        socket = null;
        in = null;
        out = null;
    }

    public synchronized NetResponse register(
        String username,
        String password,
        String nickName,
        String email,
        String gender
    ) {
        return request("REGISTER", username, password, nickName, email, gender);
    }

    public synchronized NetResponse login(String username, String password) {
        NetResponse response = request("LOGIN", username, password);
        if (response.ok) {
            loggedUsername = username;
        }
        return response;
    }

    public synchronized NetResponse updateNickname(String newNickname) {
        return request("UPDATE_NICKNAME", newNickname);
    }

    public synchronized void logout() {
        if (isConnected()) {
            request("LOGOUT");
        }
        loggedUsername = null;
    }

    public synchronized NetResponse syncProfile(User user) {
        if (!isConnected() || user == null) {
            return NetResponse.offline();
        }
        return request(
            "SYNC_PROFILE",
            String.valueOf(user.getCoins()),
            String.valueOf(user.getGems()),
            String.valueOf(user.getLastLevel()),
            String.valueOf(user.getLastSeason()),
            String.valueOf(user.getMinigamesCompleted()),
            String.valueOf(user.getDailyQuestsCompletedCount()),
            String.valueOf(user.getOtherQuestsCompletedCount()),
            String.valueOf(user.getMaxMewPoint())
        );
    }

    public synchronized NetResponse submitBonusScore(int score) {
        return request("SUBMIT_BONUS_SCORE", String.valueOf(score));
    }

    public synchronized List<UserSnapshot> getLeaderboard() {
        NetResponse response = request("GET_LEADERBOARD");
        List<UserSnapshot> rows = new ArrayList<>();
        if (!response.ok || response.payload == null || response.payload.isBlank()) {
            return rows;
        }
        for (String row : response.payload.split(";", -1)) {
            if (!row.isBlank()) {
                rows.add(UserSnapshot.fromWire(row));
            }
        }
        return rows;
    }

    public synchronized List<String> getOnlineUsers() {
        NetResponse response = request("GET_ONLINE_USERS");
        List<String> names = new ArrayList<>();
        if (!response.ok || response.payload == null || response.payload.isBlank()) {
            return names;
        }
        for (String name : response.payload.split(",", -1)) {
            if (!name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    public User applyLoginSnapshot(UserSnapshot snap) {
        UserManager um = UserManager.getInstance();
        User existing = null;
        for (User user : um.users) {
            if (user.getUsername().equalsIgnoreCase(snap.username)) {
                existing = user;
                break;
            }
        }
        if (existing == null) {
            existing = new User(
                snap.username,
                snap.passwordHash,
                snap.nickName,
                snap.email,
                "female".equalsIgnoreCase(snap.gender) ? Gender.FEMALE : Gender.MALE
            );
            um.users.add(existing);
        }
        snap.applyTo(existing);
        um.login(existing);
        return existing;
    }

    private NetResponse request(String type, String... parts) {
        if (!isConnected()) {
            return NetResponse.offline();
        }
        try {
            out.println(UserSnapshot.join(prepend(type, parts)));
            String line = in.readLine();
            if (line == null) {
                disconnect();
                return NetResponse.fail("Disconnected from server.");
            }
            return NetResponse.parse(line);
        } catch (IOException e) {
            disconnect();
            return NetResponse.fail("Server connection lost.");
        }
    }

    private static String[] prepend(String type, String[] parts) {
        String[] all = new String[parts.length + 1];
        all[0] = type;
        System.arraycopy(parts, 0, all, 1, parts.length);
        return all;
    }

    public String getLoggedUsername() {
        return loggedUsername;
    }
}
