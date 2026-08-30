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
    private final MatchmakingService matchmaking;
    private String username;
    private volatile PrintWriter out;

    public ClientHandler(Socket socket, UserStore store, SessionManager sessions, MatchmakingService matchmaking) {
        this.socket = socket;
        this.store = store;
        this.sessions = sessions;
        this.matchmaking = matchmaking;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /** Pushes an unsolicited server -> client message, tagged as an EVENT. */
    public void sendEvent(String... parts) {
        PrintWriter writer = out;
        if (writer == null) {
            return;
        }
        String[] all = new String[parts.length + 1];
        all[0] = "EVENT";
        System.arraycopy(parts, 0, all, 1, parts.length);
        writer.println(UserSnapshot.join(all));
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            this.out = writer;
            String line;
            while ((line = in.readLine()) != null) {
                writer.println(handle(line));
            }
        } catch (Exception ignored) {
        } finally {
            this.out = null;
            sessions.unbind(this);
            matchmaking.handleDisconnect(this);
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
                case "CHALLENGE" -> challenge(p);
                case "CHALLENGE_RESPOND" -> challengeRespond(p);
                case "QUEUE_JOIN" -> queueJoin();
                case "QUEUE_CANCEL" -> queueCancel();
                case "MATCH_MSG" -> matchMessage(p);
                case "MATCH_LEAVE" -> matchLeave(p);
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
        matchmaking.handleDisconnect(this);
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

    private String challenge(String[] p) {
        if (username == null) {
            return err("Not logged in.");
        }
        String target = p[1];
        String role = p[2];
        if (!store.exists(target)) {
            return err("No such user.");
        }
        String error = matchmaking.challenge(this, username, target, role);
        return error == null ? ok("") : err(error);
    }

    private String challengeRespond(String[] p) {
        if (username == null) {
            return err("Not logged in.");
        }
        String challenger = p[1];
        boolean accept = "accept".equalsIgnoreCase(p[2]);
        String error = matchmaking.respondChallenge(this, username, challenger, accept);
        return error == null ? ok("") : err(error);
    }

    private String queueJoin() {
        if (username == null) {
            return err("Not logged in.");
        }
        String error = matchmaking.joinQueue(this, username);
        return error == null ? ok("") : err(error);
    }

    private String queueCancel() {
        matchmaking.cancelQueue(this);
        return ok("");
    }

    private String matchMessage(String[] p) {
        String matchId = p[1];
        String kind = p[2];
        String payload = p.length > 3 ? UserSnapshot.joinFrom(p, 3) : "";
        String error = matchmaking.relay(this, matchId, kind, payload);
        return error == null ? ok("") : err(error);
    }

    private String matchLeave(String[] p) {
        matchmaking.leaveMatch(this, p[1]);
        return ok("");
    }

    private static String ok(String payload) {
        return UserSnapshot.join("OK", payload == null ? "" : payload);
    }

    private static String err(String message) {
        return UserSnapshot.join("ERR", message);
    }
}
