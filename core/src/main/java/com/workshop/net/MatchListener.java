package com.workshop.net;

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
