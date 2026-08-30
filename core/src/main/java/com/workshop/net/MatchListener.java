package com.workshop.net;

/**
 * Receives server-pushed ("EVENT ...") messages related to matchmaking and
 * live matches. All callbacks fire on {@link GameClient}'s background
 * network thread — implementations that touch LibGDX/UI state must marshal
 * back to the render thread themselves (e.g. {@code Gdx.app.postRunnable}).
 */
public interface MatchListener {
    default void onChallengeInvite(String fromUsername, String fromRole) {
    }

    default void onChallengeDeclined(String byUsername) {
    }

    default void onMatchFound(String matchId, String opponentUsername, String yourRole, boolean isHost) {
    }

    default void onMatchMessage(String matchId, String kind, String payload) {
    }

    default void onOpponentLeft(String matchId) {
    }
}
