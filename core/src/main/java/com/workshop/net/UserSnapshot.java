package com.workshop.net;

import com.workshop.model.user.Gender;
import com.workshop.model.user.User;

public final class UserSnapshot {
    public String username;
    public String passwordHash;
    public String nickName;
    public String email;
    public String gender;
    public int coins;
    public int gems;
    public int maxMewPoint;
    public int lastLevel;
    public int lastSeason;
    public int minigamesCompleted;
    public int dailyQuests;
    public int otherQuests;
    public boolean hasNetworkBonusScore;
    public int securityQuestionId;
    public String securityAnswerHash;

    public static UserSnapshot fromUser(User user) {
        UserSnapshot snap = new UserSnapshot();
        snap.username = user.getUsername();
        snap.passwordHash = user.getPassword();
        snap.nickName = user.getNickName();
        snap.email = user.getEmail();
        snap.gender = user.getGender() == Gender.FEMALE ? "female" : "male";
        snap.coins = user.getCoins();
        snap.gems = user.getGems();
        snap.maxMewPoint = user.getMaxMewPoint();
        snap.lastLevel = user.getLastLevel();
        snap.lastSeason = user.getLastSeason();
        snap.minigamesCompleted = user.getMinigamesCompleted();
        snap.dailyQuests = user.getDailyQuestsCompletedCount();
        snap.otherQuests = user.getOtherQuestsCompletedCount();
        snap.hasNetworkBonusScore = user.hasNetworkBonusScore();
        return snap;
    }

    public void applyTo(User user) {
        if (username != null && !username.isBlank()) {
            user.setUsername(username);
        }

        if (username != null && !username.isBlank()) {
            user.setUsername(username);
        }

        if (nickName != null && !nickName.isBlank()) {
            user.setNickName(nickName);
        }

        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }

        user.setCoins(coins);
        user.setGems(gems);
        user.setMaxMewPoint(maxMewPoint);
        user.setLastLevel(lastLevel);
        user.setLastSeason(lastSeason);
        user.setMinigamesCompleted(minigamesCompleted);
        user.setDailyQuestsCompletedCount(dailyQuests);
        user.setOtherQuestsCompletedCount(otherQuests);
        user.setHasNetworkBonusScore(hasNetworkBonusScore);
    }

    public String toWire() {
        return join(
            username, passwordHash, nickName, email, gender,
            String.valueOf(coins), String.valueOf(gems), String.valueOf(maxMewPoint),
            String.valueOf(lastLevel), String.valueOf(lastSeason),
            String.valueOf(minigamesCompleted), String.valueOf(dailyQuests),
            String.valueOf(otherQuests), hasNetworkBonusScore ? "1" : "0",
            String.valueOf(securityQuestionId), securityAnswerHash
        );
    }

    public static UserSnapshot fromWire(String wire) {
        String[] p = split(wire);
        UserSnapshot snap = new UserSnapshot();
        snap.username = p[0];
        snap.passwordHash = p[1];
        snap.nickName = p[2];
        snap.email = p[3];
        snap.gender = p[4];
        snap.coins = Integer.parseInt(p[5]);
        snap.gems = Integer.parseInt(p[6]);
        snap.maxMewPoint = Integer.parseInt(p[7]);
        snap.lastLevel = Integer.parseInt(p[8]);
        snap.lastSeason = Integer.parseInt(p[9]);
        snap.minigamesCompleted = Integer.parseInt(p[10]);
        snap.dailyQuests = Integer.parseInt(p[11]);
        snap.otherQuests = Integer.parseInt(p[12]);
        snap.hasNetworkBonusScore = "1".equals(p[13]);
        snap.securityQuestionId =
            p.length > 14 && !p[14].isBlank()
                ? Integer.parseInt(p[14])
                : 0;

        snap.securityAnswerHash =
            p.length > 15 ? p[15] : "";
        return snap;
    }

    public static String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append('\u001f');
            }
            sb.append(parts[i] == null ? "" : parts[i].replace("\n", " ").replace("\r", " "));
        }
        return sb.toString();
    }

    public static String[] split(String wire) {
        return wire.split("\u001f", -1);
    }

    public static String joinFrom(String[] parts, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < parts.length; i++) {
            if (i > start) {
                sb.append('\u001f');
            }
            sb.append(parts[i] == null ? "" : parts[i]);
        }
        return sb.toString();
    }
}
