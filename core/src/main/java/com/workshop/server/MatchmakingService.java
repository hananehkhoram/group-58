package com.workshop.server;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side half of the networked "I, Zombie" mini-game: lets a client
 * challenge a specific online user or join a random queue, pairs two
 * clients into a match, and relays opaque match messages between them.
 * The server never looks at what's inside a relayed message — it is only
 * a switchboard, matching the "clients don't talk directly" requirement.
 */
public final class MatchmakingService {

    private static final class PendingChallenge {
        final String fromUsername;
        final String fromRole;
        final ClientHandler fromHandler;

        PendingChallenge(String fromUsername, String fromRole, ClientHandler fromHandler) {
            this.fromUsername = fromUsername;
            this.fromRole = fromRole;
            this.fromHandler = fromHandler;
        }
    }

    private static final class QueueEntry {
        final String username;
        final ClientHandler handler;

        QueueEntry(String username, ClientHandler handler) {
            this.username = username;
            this.handler = handler;
        }
    }

    private static final class MatchSession {
        final String matchId;
        final String usernameA;
        final ClientHandler handlerA;
        final String roleA;
        final String usernameB;
        final ClientHandler handlerB;
        final String roleB;
        final String hostUsername;

        MatchSession(
            String matchId,
            String usernameA, ClientHandler handlerA, String roleA,
            String usernameB, ClientHandler handlerB, String roleB,
            String hostUsername
        ) {
            this.matchId = matchId;
            this.usernameA = usernameA;
            this.handlerA = handlerA;
            this.roleA = roleA;
            this.usernameB = usernameB;
            this.handlerB = handlerB;
            this.roleB = roleB;
            this.hostUsername = hostUsername;
        }

        ClientHandler other(ClientHandler handler) {
            return handler == handlerA ? handlerB : handlerA;
        }

        String usernameOf(ClientHandler handler) {
            return handler == handlerA ? usernameA : usernameB;
        }
    }

    private final SessionManager sessions;
    private final Object lock = new Object();

    private final Map<String, PendingChallenge> pendingChallengesByTarget = new ConcurrentHashMap<>();
    private final Deque<QueueEntry> randomQueue = new ArrayDeque<>();
    private final Map<String, MatchSession> matchesById = new ConcurrentHashMap<>();
    private final Map<ClientHandler, String> matchIdByHandler = new ConcurrentHashMap<>();

    public MatchmakingService(SessionManager sessions) {
        this.sessions = sessions;
    }

    public String challenge(ClientHandler fromHandler, String fromUsername, String targetUsername, String fromRole) {
        if (matchIdByHandler.containsKey(fromHandler)) {
            return "You are already in a match.";
        }
        ClientHandler targetHandler = sessions.handlerFor(targetUsername);
        if (targetHandler == null) {
            return "User is not online.";
        }
        if (targetHandler == fromHandler) {
            return "You cannot challenge yourself.";
        }
        pendingChallengesByTarget.put(targetUsername.toLowerCase(), new PendingChallenge(fromUsername, fromRole, fromHandler));
        targetHandler.sendEvent("CHALLENGE_INVITE", fromUsername, fromRole);
        return null;
    }

    public String respondChallenge(ClientHandler fromHandler, String respondingUsername, String challengerUsername, boolean accept) {
        PendingChallenge pending = pendingChallengesByTarget.remove(respondingUsername.toLowerCase());
        if (pending == null || !pending.fromUsername.equalsIgnoreCase(challengerUsername)) {
            return "No pending challenge from that user.";
        }
        if (!accept) {
            pending.fromHandler.sendEvent("CHALLENGE_DECLINED", respondingUsername);
            return null;
        }
        if (!pending.fromHandler.isConnected() || matchIdByHandler.containsKey(pending.fromHandler)
            || matchIdByHandler.containsKey(fromHandler)) {
            return "That user is no longer available.";
        }
        String hostRole = pending.fromRole;
        String guestRole = "PLANT".equalsIgnoreCase(hostRole) ? "ZOMBIE" : "PLANT";
        createMatch(pending.fromUsername, pending.fromHandler, hostRole, respondingUsername, fromHandler, guestRole);
        return null;
    }

    public String joinQueue(ClientHandler handler, String username) {
        synchronized (lock) {
            if (matchIdByHandler.containsKey(handler)) {
                return "You are already in a match.";
            }
            for (QueueEntry entry : randomQueue) {
                if (entry.handler == handler) {
                    return "You are already in the queue.";
                }
            }
            QueueEntry waiting = randomQueue.pollFirst();
            if (waiting == null || !waiting.handler.isConnected()) {
                randomQueue.addLast(new QueueEntry(username, handler));
                return null;
            }
            String hostRole = Math.random() < 0.5 ? "PLANT" : "ZOMBIE";
            String guestRole = "PLANT".equals(hostRole) ? "ZOMBIE" : "PLANT";
            createMatch(waiting.username, waiting.handler, hostRole, username, handler, guestRole);
            return null;
        }
    }

    public void cancelQueue(ClientHandler handler) {
        synchronized (lock) {
            randomQueue.removeIf(entry -> entry.handler == handler);
        }
    }

    private void createMatch(
        String hostUsername, ClientHandler hostHandler, String hostRole,
        String guestUsername, ClientHandler guestHandler, String guestRole
    ) {
        String matchId = UUID.randomUUID().toString();
        MatchSession session = new MatchSession(
            matchId, hostUsername, hostHandler, hostRole, guestUsername, guestHandler, guestRole, hostUsername
        );
        matchesById.put(matchId, session);
        matchIdByHandler.put(hostHandler, matchId);
        matchIdByHandler.put(guestHandler, matchId);

        hostHandler.sendEvent("MATCH_FOUND", matchId, guestUsername, hostRole, "1");
        guestHandler.sendEvent("MATCH_FOUND", matchId, hostUsername, guestRole, "0");
    }

    public String relay(ClientHandler fromHandler, String matchId, String kind, String payload) {
        MatchSession session = matchesById.get(matchId);
        if (session == null || !matchId.equals(matchIdByHandler.get(fromHandler))) {
            return "Not part of that match.";
        }
        ClientHandler other = session.other(fromHandler);
        other.sendEvent("MATCH_MSG", matchId, kind, payload);
        if ("END".equals(kind)) {
            endMatch(matchId);
        }
        return null;
    }

    public void leaveMatch(ClientHandler handler, String matchId) {
        MatchSession session = matchesById.get(matchId);
        if (session == null) {
            return;
        }
        ClientHandler other = session.other(handler);
        if (other != null) {
            other.sendEvent("OPPONENT_LEFT", matchId);
        }
        endMatch(matchId);
    }

    private void endMatch(String matchId) {
        MatchSession session = matchesById.remove(matchId);
        if (session != null) {
            matchIdByHandler.remove(session.handlerA);
            matchIdByHandler.remove(session.handlerB);
        }
    }

    public void handleDisconnect(ClientHandler handler) {
        synchronized (lock) {
            randomQueue.removeIf(entry -> entry.handler == handler);
        }
        pendingChallengesByTarget.entrySet().removeIf(e -> e.getValue().fromHandler == handler);

        String matchId = matchIdByHandler.get(handler);
        if (matchId != null) {
            leaveMatch(handler, matchId);
        }
    }
}
