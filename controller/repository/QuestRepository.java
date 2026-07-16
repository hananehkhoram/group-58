package controller.repository;

import model.Quest;
import model.plants.PlantFamily;
import model.season.Season;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QuestRepository implements AssetRepository<Quest> {
    private final Map<String, Quest> questMap = new HashMap<>();

    @Override
    public void load(String filePath) {
        add("sun-collector-3000", "Daily Sun Harvester", Quest.QuestCategory.DAILY,
                "Collect 3000 suns in a single day", Quest.RewardType.COIN, 30, Quest.QuestLevel.MEDIUM, 3000);
        add("sun-collector-4000", "Daily Sun Harvester", Quest.QuestCategory.DAILY,
                "Collect 4000 suns in a single day", Quest.RewardType.COIN, 40, Quest.QuestLevel.MEDIUM, 4000);
        add("sun-collector-5000", "Daily Sun Harvester", Quest.QuestCategory.DAILY,
                "Collect 5000 suns in a single day", Quest.RewardType.COIN, 50, Quest.QuestLevel.MEDIUM, 5000);

        for (Season s : DataManager.getInstance().seasons.getMainChapters()) {
            add("chapter-hunter-" + s.getName(), s.getName() + " Hunter", Quest.QuestCategory.MAIN,
                    "Defeat 50 zombies from the " + s.getName() + " season", Quest.RewardType.SEED_PACKET, 10, Quest.QuestLevel.HIGH, 50);
        }

        add("plant-pro-killer", "Plant Pro", Quest.QuestCategory.DAILY,
                "Kill 10 zombies using only a single plant", Quest.RewardType.RANDOM_PLANT, 1, Quest.QuestLevel.HIGH, 1);
        add("only-cactus", "Only Cactus", Quest.QuestCategory.DAILY,
                "Kill 10 zombies using only Cactus", Quest.RewardType.GEM, 20, Quest.QuestLevel.HIGH, 1);

        for (int n = 0; n <= 5; n++) {
            add("economic-herbivore-" + n, "Thrifty Herbivore", Quest.QuestCategory.MAIN,
                    "Win without losing more than " + n + " plants",
                    Quest.RewardType.SEED_PACKET, 20 - n, Quest.QuestLevel.HIGH, 1);
        }

        add("defense-master", "Defense Master", Quest.QuestCategory.EPIC,
                "Complete a level with exactly zero sun left", Quest.RewardType.GEM, 200, Quest.QuestLevel.CRITICAL, 1);
        add("quick-action", "Quick Reflexes", Quest.QuestCategory.MAIN,
                "Kill 10 zombies in less than 30 seconds from the start of the first wave", Quest.RewardType.COIN, 500, Quest.QuestLevel.MEDIUM, 1);
        add("pro-destroyer", "Pro Destroyer", Quest.QuestCategory.DAILY,
                "Use 3 explosive plants in a single level", Quest.RewardType.COIN, 100, Quest.QuestLevel.LOW, 1);
        add("symmetry", "Symmetry", Quest.QuestCategory.DAILY,
                "The lawn must be symmetrical at the end of the level", Quest.RewardType.COIN, 500, Quest.QuestLevel.HIGH, 1);

        for (PlantFamily f : PlantFamily.values()) {
            add("family-kill-" + f, "Family Slay", Quest.QuestCategory.DAILY,
                    "Kill zombies using only the " + f + " family", Quest.RewardType.COIN, 1000, Quest.QuestLevel.MEDIUM, 1);
            add("family-avoid-" + f, "Thriving in Constraints", Quest.QuestCategory.DAILY,
                    "Win without using the " + f + " family", Quest.RewardType.GEM, 100, Quest.QuestLevel.HIGH, 1);
        }

        add("night-or-morning", "Night or Morning", Quest.QuestCategory.EPIC,
                "Complete a night level using only mushroom plants", Quest.RewardType.GEM, 20, Quest.QuestLevel.HIGH, 1);
        add("win-streak-5", "Win Streak", Quest.QuestCategory.DAILY,
                "Win 5 consecutive levels on the highest difficulty", Quest.RewardType.COIN, 5000, Quest.QuestLevel.MEDIUM, 1);
        add("almost-lost", "Close Call", Quest.QuestCategory.DAILY,
                "Kill 10 zombies in the first column of a row without using a lawnmover", Quest.RewardType.COIN, 300, Quest.QuestLevel.MEDIUM, 1);
        add("ocd", "OCD? What's That?", Quest.QuestCategory.DAILY,
                "Win with zero symmetry on the lawn (except the middle row)", Quest.RewardType.COIN, 800, Quest.QuestLevel.MEDIUM, 1);
        add("cloudy-day", "Cloudy Day", Quest.QuestCategory.DAILY,
                "Win using only 3 sun-producing plants", Quest.RewardType.GEM, 10, Quest.QuestLevel.HIGH, 1);

        for (int col = 0; col < 9; col++) {
            add("one-less-column-" + col, "One Less Column", Quest.QuestCategory.DAILY,
                    "Win without planting in column " + col, Quest.RewardType.GEM, 10, Quest.QuestLevel.HIGH, 1);
        }
        for (int row = 0; row < 5; row++) {
            add("undefended-row-" + row, "Undefended Row", Quest.QuestCategory.DAILY,
                    "Win without planting in row " + row, Quest.RewardType.GEM, 20, Quest.QuestLevel.HIGH, 1);
        }
        add("undefended-cross", "Undefended Cross", Quest.QuestCategory.DAILY,
                "Win with the specified row and column left empty", Quest.RewardType.GEM, 25, Quest.QuestLevel.HIGH, 1);

        for (int n : new int[]{10, 20, 30, 40, 50}) {
            add("lawnmower-time-" + n, "Lawnmower Time", Quest.QuestCategory.EPIC,
                    "Kill at least " + n + " zombies using lawnmowers", Quest.RewardType.GEM, n, Quest.QuestLevel.MEDIUM, n);
        }
    }

    private void add(String id, String name, Quest.QuestCategory cat, String desc,
                     Quest.RewardType rt, int amount, Quest.QuestLevel level, int targetProgress) {
        questMap.put(id, new Quest(id, name, cat, desc, rt, amount, level, targetProgress));
    }

    @Override
    public Quest get(String id) {
        return questMap.get(id);
    }

    public Collection<Quest> getAll() {
        return questMap.values();
    }

    public java.util.List<Quest> getByCategory(Quest.QuestCategory category) {
        return questMap.values().stream().filter(q -> q.getCategory() == category).toList();
    }
}