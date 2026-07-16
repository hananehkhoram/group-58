package model;

import java.util.ArrayList;
import model.plants.PlantFamily;


public class Quest {
    public enum QuestCategory { DAILY, MAIN, EPIC }
    public enum QuestLevel { LOW, MEDIUM, HIGH, CRITICAL }
    public enum RewardType { COIN, GEM, SEED_PACKET, RANDOM_PLANT }

    private final String id;
    private final String name;
    private final QuestCategory category;
    private final String description;
    private final RewardType rewardType;
    private final int rewardAmount;
    private final QuestLevel priority;
    private final int targetProgress;

    public Quest(String id, String name, QuestCategory category, String description,
                 RewardType rewardType, int rewardAmount, QuestLevel priority, int targetProgress) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
        this.priority = priority;
        this.targetProgress = targetProgress;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public QuestCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public RewardType getRewardType() { return rewardType; }
    public int getRewardAmount() { return rewardAmount; }
    public QuestLevel getPriority() { return priority; }
    public int getTargetProgress() { return targetProgress; }
}