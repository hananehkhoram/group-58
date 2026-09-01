package com.workshop.model.zombie;

import java.util.Map;
import java.util.Set;

public final class BossZombieRegistry {

    private BossZombieRegistry() {}

    public static final String EGYPT_BOSS_ID = "ZombieBossEgypt";
    public static final String FROZEN_CAVES_BOSS_ID = "ZombieBossFrozenCaves";
    public static final String BIG_WAVE_BEACH_BOSS_ID = "ZombieBossBigWaveBeach";
    public static final String DARK_AGES_BOSS_ID = "ZombieBossDarkAges";

    public static final String EGYPT_BOSS_NAME = "AncientEgyptZomboss";
    public static final String FROZEN_CAVES_BOSS_NAME = "FrozenCavesZomboss";
    public static final String BIG_WAVE_BEACH_BOSS_NAME = "BigWaveBeachZomboss";
    public static final String DARK_AGES_BOSS_NAME = "DarkAgesZomboss";

    private static final Map<String, String> BOSS_NAME_BY_SEASON = Map.of(
        "Ancient Egypt", EGYPT_BOSS_NAME,
        "FrozenCave", FROZEN_CAVES_BOSS_NAME,
        "Big Wave Beach", BIG_WAVE_BEACH_BOSS_NAME,
        "Dark Ages", DARK_AGES_BOSS_NAME
    );

    private static final Set<String> BOSS_IDS = Set.of(
        EGYPT_BOSS_ID, FROZEN_CAVES_BOSS_ID, BIG_WAVE_BEACH_BOSS_ID, DARK_AGES_BOSS_ID
    );

    private static final Map<String, String> SEASON_BY_BOSS_NAME = Map.of(
        EGYPT_BOSS_NAME, "Ancient Egypt",
        FROZEN_CAVES_BOSS_NAME, "FrozenCave",
        BIG_WAVE_BEACH_BOSS_NAME, "Big Wave Beach",
        DARK_AGES_BOSS_NAME, "Dark Ages"
    );

    public static String bossNameForSeason(String seasonName) {
        return seasonName == null ? null : BOSS_NAME_BY_SEASON.get(seasonName);
    }

    public static boolean isBossId(String id) {
        return id != null && BOSS_IDS.contains(id);
    }

    public static boolean isBossName(String name) {
        return name != null && SEASON_BY_BOSS_NAME.containsKey(name);
    }

    public static boolean isAllowedInSeason(String zombieName, String seasonName) {
        String requiredSeason = SEASON_BY_BOSS_NAME.get(zombieName);
        if (requiredSeason == null) {
            return true;
        }
        return requiredSeason.equals(seasonName);
    }
}
