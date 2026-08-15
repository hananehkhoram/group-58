package com.workshop.view.gameplay;

import com.workshop.model.GameContext;
import com.workshop.model.level.LevelType;
import com.workshop.model.zombie.Zombie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class ZombieIntroZombieProvider {

    private ZombieIntroZombieProvider() {
    }

    public static List<Zombie> getZombies(
        GameContext gameContext
    ) {
        Map<String, Zombie> allZombies =
            gameContext.getDataManager()
                .zombies
                .getZombieDataMap();

        List<Zombie> result = new ArrayList<>();

        for (Map.Entry<String, Zombie> entry
            : allZombies.entrySet()) {

            Zombie zombie = entry.getValue();

            if (isAvailable(gameContext, zombie)) {
                result.add(zombie);
            }
        }

        result.sort(
            Comparator.comparing(Zombie::getName)
        );

        return result;
    }

    private static boolean isAvailable(
        GameContext gameContext,
        Zombie zombie
    ) {
        if (gameContext.getLevel().getLevelType()
            == LevelType.Zombotany_MG) {

            return zombie.getId() != null
                && zombie.getId().startsWith(
                "ZombieZombotany"
            );
        }

        String seasonName =
            gameContext.getSeason().getName();

        return gameContext
            .getDataManager()
            .zombies
            .isAvailableInChapter(
                zombie.getName(),
                seasonName
            );
    }
}
