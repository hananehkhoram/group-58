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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class GameClient {
    public static final int DEFAULT_PORT = 5454;
    public static final String DEFAULT_HOST = "127.0.0.1";

    private static final GameClient INSTANCE = new GameClient();
    private static final long REQUEST_TIMEOUT_SECONDS = 5;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String loggedUsername;
    private Thread readerThread;
    private volatile MatchListener matchListener;

    private final BlockingQueue<String> pendingResponses = new LinkedBlockingQueue<>();

    public static GameClient get() {
        return INSTANCE;
    }

    public void setMatchListener(MatchListener listener) {
        this.matchListener = listener;
    }

    public synchronized boolean connect(String host, int port) {
        disconnect();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 1500);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            startReaderThread();
            NetResponse ping = request("PING");
            return ping.ok;
        } catch (Exception e) {
            disconnect();
            return false;
        }
    }

    private void startReaderThread() {
        readerThread = new Thread(this::readLoop, "game-client-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop() {
        try {
            String line;
            while (in != null && (line = in.readLine()) != null) {
                dispatch(line);
            }
        } catch (IOException ignored) {
        } finally {
            pendingResponses.offer("\u0000DISCONNECTED");
        }
    }

    private void dispatch(String line) {
        String[] parts = UserSnapshot.split(line);
        if (parts.length > 0 && "EVENT".equals(parts[0])) {
            handleEvent(parts);
        } else {
            pendingResponses.offer(line);
        }
    }

    private void handleEvent(String[] parts) {
        MatchListener listener = matchListener;
        if (listener == null || parts.length < 2) {
            return;
        }
        String type = parts[1];
        try {
            switch (type) {
                case "CHALLENGE_INVITE" -> listener.onChallengeInvite(parts[2], parts[3]);
                case "CHALLENGE_DECLINED" -> listener.onChallengeDeclined(parts[2]);
                case "MATCH_FOUND" -> listener.onMatchFound(parts[2], parts[3], parts[4], "1".equals(parts[5]));
                case "MATCH_MSG" -> listener.onMatchMessage(
                    parts[2], parts[3], parts.length > 4 ? UserSnapshot.joinFrom(parts, 4) : ""
                );
                case "OPPONENT_LEFT" -> listener.onOpponentLeft(parts[2]);
                default -> {
                }
            }
        } catch (Exception ignored) {
            // A malformed/short event should never crash the reader thread.
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
        pendingResponses.clear();
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

    public synchronized NetResponse updateNickname(String newNickname) {
        if (!isConnected()) {
            return NetResponse.offline();
        }
        return request("UPDATE_NICKNAME", newNickname == null ? "" : newNickname);
    }

    public synchronized NetResponse updateEmail(String newEmail) {
        if (!isConnected()) {
            return NetResponse.offline();
        }

        return request(
            "UPDATE_EMAIL",
            newEmail == null ? "" : newEmail
        );
    }

    public synchronized NetResponse updateUsername(String newUsername) {
        if (!isConnected()) {
            return NetResponse.offline();
        }

        NetResponse response = request(
            "UPDATE_USERNAME",
            newUsername == null ? "" : newUsername
        );

        if (response.ok) {
            loggedUsername = newUsername;
        }

        return response;
    }

    public synchronized NetResponse updatePassword(
        String oldPassword,
        String newPassword
    ) {
        if (!isConnected()) {
            return NetResponse.offline();
        }

        return request(
            "UPDATE_PASSWORD",
            oldPassword,
            newPassword
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

    public synchronized NetResponse challenge(String targetUsername, String role) {
        return request("CHALLENGE", targetUsername, role);
    }

    public synchronized NetResponse respondToChallenge(String challengerUsername, boolean accept) {
        return request("CHALLENGE_RESPOND", challengerUsername, accept ? "accept" : "decline");
    }

    public synchronized NetResponse joinRandomQueue() {
        return request("QUEUE_JOIN");
    }

    public synchronized NetResponse cancelRandomQueue() {
        return request("QUEUE_CANCEL");
    }

    public synchronized NetResponse sendMatchAction(String matchId, String payload) {
        return request("MATCH_MSG", matchId, "ACTION", payload);
    }

    public synchronized NetResponse sendMatchState(String matchId, String payload) {
        return request("MATCH_MSG", matchId, "STATE", payload);
    }

    public synchronized NetResponse sendMatchReaction(String matchId, String payload) {
        return request("MATCH_MSG", matchId, "REACTION", payload);
    }

    public synchronized NetResponse sendMatchEnd(String matchId, String payload) {
        return request("MATCH_MSG", matchId, "END", payload);
    }

    public synchronized NetResponse leaveMatch(String matchId) {
        return request("MATCH_LEAVE", matchId);
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
            String line = pendingResponses.poll(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (line == null) {
                return NetResponse.fail("Server did not respond in time.");
            }
            if ("\u0000DISCONNECTED".equals(line)) {
                disconnect();
                return NetResponse.fail("Disconnected from server.");
            }
            return NetResponse.parse(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return NetResponse.fail("Request interrupted.");
        }
    }

    private static String[] prepend(String type, String[] parts) {
        String[] all = new String[parts.length + 1];
        all[0] = type;
        System.arraycopy(parts, 0, all, 1, parts.length);
        return all;
    }

    public synchronized NetResponse setSecurityQuestion(
        String username,
        String password,
        int questionId,
        String answer
    ) {
        if (!isConnected()) {
            return NetResponse.offline();
        }

        return request(
            "SET_SECURITY",
            username,
            password,
            String.valueOf(questionId),
            answer
        );
    }

    public synchronized NetResponse forgotPasswordStart(
        String username,
        String email
    ) {
        if (!isConnected()) {
            return NetResponse.offline();
        }

        return request(
            "FORGOT_START",
            username,
            email
        );
    }

    public synchronized NetResponse forgotPasswordReset(
        String username,
        String email,
        String answer,
        String newPassword
    ) {
        if (!isConnected()) {
            return NetResponse.offline();
        }

        return request(
            "FORGOT_RESET",
            username,
            email,
            answer,
            newPassword
        );
    }

    public synchronized NetResponse forgotPasswordAnswer(
        String username,
        String email,
        String answer
    ) {
        if (!isConnected()) {
            return NetResponse.offline();
        }

        return request(
            "FORGOT_ANSWER",
            username,
            email,
            answer
        );
    }

    public String getLoggedUsername() {
        return loggedUsername;
    }
}
