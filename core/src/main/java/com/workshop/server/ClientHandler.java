package com.workshop.server;

import com.workshop.model.user.Security;
import com.workshop.model.user.SecurityQuestions;
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
                case "UPDATE_EMAIL" -> updateEmail(p);
                case "UPDATE_PASSWORD" -> updatePassword(p);
                case "UPDATE_USERNAME" -> updateUsername(p);
                case "GET_LEADERBOARD" -> leaderboard();
                case "SUBMIT_BONUS_SCORE" -> submitBonus(p);
                case "GET_ONLINE_USERS" -> ok(String.join(",", sessions.onlineUsernames()));
                case "CHALLENGE" -> challenge(p);
                case "CHALLENGE_RESPOND" -> challengeRespond(p);
                case "QUEUE_JOIN" -> queueJoin();
                case "QUEUE_CANCEL" -> queueCancel();
                case "MATCH_MSG" -> matchMessage(p);
                case "MATCH_LEAVE" -> matchLeave(p);
                case "SET_SECURITY" -> setSecurity(p);
                case "FORGOT_START" -> forgotPasswordStart(p);
                case "FORGOT_RESET" -> forgotPasswordReset(p);
                case "FORGOT_ANSWER" -> forgotPasswordAnswer(p);
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

    private String updateEmail(String[] p) {
        if (username == null) {
            return err("Not logged in.");
        }

        String email = p.length > 1 ? p[1].trim() : "";

        if (!isEmailValid(email)) {
            return err("Invalid email format.");
        }

        UserSnapshot snap = store.find(username);

        if (snap == null) {
            return err("User does not exist!");
        }

        if (email.equalsIgnoreCase(snap.email)) {
            return err("Email is equal to the current email");
        }

        snap.email = email;
        store.put(snap);

        return ok(snap.toWire());
    }

    private String updatePassword(String[] p) {
        if (username == null) {
            return err("Not logged in.");
        }

        if (p.length < 3) {
            return err("Invalid password request.");
        }

        String oldPassword = p[1];
        String newPassword = p[2];

        UserSnapshot snap = store.find(username);

        if (snap == null) {
            return err("User does not exist!");
        }

        String oldPasswordHash = Security.hashPassword(oldPassword);

        if (!snap.passwordHash.equals(oldPasswordHash)
            && !snap.passwordHash.equals(oldPassword)) {
            return err("Password is incorrect.");
        }

        if (oldPassword.equals(newPassword)) {
            return err("Password is equal to the current password");
        }

        String validation = validatePassword(newPassword);

        if (!"ok".equals(validation)) {
            return err(validation);
        }

        snap.passwordHash = Security.hashPassword(newPassword);
        store.put(snap);

        return ok(snap.toWire());
    }

    private String updateUsername(String[] p) {
        if (username == null) {
            return err("Not logged in.");
        }

        String newUsername = p.length > 1 ? p[1].trim() : "";

        if (!isUsernameValid(newUsername)) {
            return err("Invalid username format.");
        }

        if (username.equalsIgnoreCase(newUsername)) {
            return err("Username is equal to the current username");
        }

        if (store.exists(newUsername)) {
            return err("Username already exists.");
        }

        String oldUsername = username;

        UserSnapshot snap = store.rename(
            oldUsername,
            newUsername
        );

        if (snap == null) {
            return err("User does not exist!");
        }

        sessions.rebind(
            oldUsername,
            newUsername,
            this
        );

        username = newUsername;

        return ok(snap.toWire());
    }

    private static boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }

        String regex =
            "^[a-zA-Z0-9](?:[a-zA-Z0-9._-]*[a-zA-Z0-9])?"
                + "@[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?"
                + "\\.[a-zA-Z]{2,}$";

        return email.matches(regex) && !email.contains("..");
    }

    private static boolean isNickNameValid(String nickName) {
        return nickName != null
            && nickName.length() >= 3
            && nickName.length() <= 30;
    }

    private static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password is too short! It must be at least 8 characters.";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter.";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }

        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit.";
        }

        String specialChars =
            ".*[!#\\$%\\^&\\*\\(\\)=\\+\\{\\}\\]\\[\\|/\\\\:;'\",<>\\?].*";

        if (!password.matches(specialChars)) {
            return "Password must contain at least one special character.";
        }

        return "ok";
    }

    private static boolean isUsernameValid(String username) {
        return username != null
            && username.matches("^[a-zA-Z0-9-]+$");
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

    private String setSecurity(String[] p) {
        if (p.length < 5) {
            return err("Invalid security question request.");
        }

        String targetUsername = p[1];
        String password = p[2];
        int questionId = Integer.parseInt(p[3]);
        String answer = p[4].trim();

        UserSnapshot snap = store.find(targetUsername);

        if (snap == null) {
            return err("User does not exist!");
        }

        String passwordHash = Security.hashPassword(password);

        if (!snap.passwordHash.equals(passwordHash)
            && !snap.passwordHash.equals(password)) {
            return err("Incorrect password.");
        }

        if (questionId < 1
            || questionId > SecurityQuestions.values().length) {
            return err("Invalid security question.");
        }

        if (answer.isBlank()) {
            return err("Security answer cannot be empty.");
        }

        snap.securityQuestionId = questionId;
        snap.securityAnswerHash =
            Security.hashPassword(answer.toLowerCase());

        store.put(snap);

        return ok("");
    }

    private String forgotPasswordStart(String[] p) {
        if (p.length < 3) {
            return err("Invalid forgot password request.");
        }

        String targetUsername = p[1].trim();
        String email = p[2].trim();

        UserSnapshot snap = store.find(targetUsername);

        if (snap == null) {
            return err("User does not exist!");
        }

        if (snap.email == null
            || !snap.email.equalsIgnoreCase(email)) {
            return err("Email is incorrect.");
        }

        if (snap.securityQuestionId <= 0) {
            return err("Security question is not set.");
        }

        return ok(String.valueOf(snap.securityQuestionId));
    }

    private String forgotPasswordReset(String[] p) {
        if (p.length < 5) {
            return err("Invalid forgot password request.");
        }

        String targetUsername = p[1].trim();
        String email = p[2].trim();
        String answer = p[3].trim();
        String newPassword = p[4];

        UserSnapshot snap = store.find(targetUsername);

        if (snap == null) {
            return err("User does not exist!");
        }

        if (snap.email == null
            || !snap.email.equalsIgnoreCase(email)) {
            return err("Email is incorrect.");
        }

        if (snap.securityAnswerHash == null
            || snap.securityAnswerHash.isBlank()) {
            return err("Security question is not set.");
        }

        String answerHash =
            Security.hashPassword(answer.toLowerCase());

        if (!snap.securityAnswerHash.equals(answerHash)) {
            return err("Security answer is incorrect.");
        }

        String validation = validatePassword(newPassword);

        if (!"ok".equals(validation)) {
            return err(validation);
        }

        snap.passwordHash =
            Security.hashPassword(newPassword);

        store.put(snap);

        return ok("");
    }

    private String forgotPasswordAnswer(String[] p) {
        if (p.length < 4) {
            return err("Invalid forgot password request.");
        }

        String targetUsername = p[1].trim();
        String email = p[2].trim();
        String answer = p[3].trim();

        UserSnapshot snap = store.find(targetUsername);

        if (snap == null) {
            return err("User does not exist!");
        }

        if (snap.email == null
            || !snap.email.equalsIgnoreCase(email)) {
            return err("Email is incorrect.");
        }

        if (snap.securityAnswerHash == null
            || snap.securityAnswerHash.isBlank()) {
            return err("Security question is not set.");
        }

        String answerHash =
            Security.hashPassword(answer.toLowerCase());

        if (!snap.securityAnswerHash.equals(answerHash)) {
            return err("Security answer is incorrect.");
        }

        return ok("");
    }

    private static String ok(String payload) {
        return UserSnapshot.join("OK", payload == null ? "" : payload);
    }

    private static String err(String message) {
        return UserSnapshot.join("ERR", message);
    }
}
