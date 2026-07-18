package model.plants.upgradeEffect;

import model.plants.Plant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeManager {

    public static void applyBehavior(Plant plant, BehaviorEffect effect) {
        if (effect != null && effect.getKey() != null) {
            plant.addBehavior(effect.getKey());
        }
    }

    public static void applyStat(Plant plant, StatEffect effect) {
        if (effect == null || effect.getKey() == null) return;

        double amount = effect.getAmount();

        switch (effect.getKey()) {
            case HP:
                plant.setBaseHp((int) (plant.getBaseHp() + amount));
                plant.heal((int) amount);
                break;

            case DMG:
                try {
                    int currentDmg = Integer.parseInt(plant.getDamage());
                    plant.setDamage(String.valueOf(currentDmg + (int) amount));
                } catch (NumberFormatException e) {
                    plant.setDamage(String.valueOf((int) amount));
                }
                break;

            case COOLDOWN:
                double newCooldown = plant.getRechargeTime() + amount;
                plant.setRechargeTime(Math.max(newCooldown, 0));
                break;

            case COST:
                int newCost = plant.getSunCost() + (int) amount;
                plant.setSunCost(Math.max(newCost, 0));
                break;

            case ATK_SPEED:
            case PROD_TIME:
            case ARM_TIME:
            case DIGEST:
            case EAT_TIME:
            case CHARGE_TIME:
                if (plant.getActionInterval() != null) {
                    double newInterval = plant.getActionInterval() + amount;
                    plant.setActionInterval(Math.max(newInterval, 0));
                }
                break;

            default:
                Map<String, String> params = plant.getAbilityParams();
                if (params == null) {
                    params = new HashMap<>();
                    plant.setAbilityParams(params);
                }

                String keyStr = effect.getKey().name();
                try {
                    if (params.containsKey(keyStr)) {
                        double currentVal = Double.parseDouble(params.get(keyStr));
                        params.put(keyStr, String.valueOf(currentVal + amount));
                    } else {
                        params.put(keyStr, String.valueOf(amount));
                    }
                } catch (NumberFormatException e) {
                    params.put(keyStr, String.valueOf(amount));
                }
                break;
        }
    }

    public static void applyUpgrades(Plant plant, List<StatEffect> stats, List<BehaviorEffect> behaviors) {
        if (stats != null) {
            for (StatEffect stat : stats) {
                applyStat(plant, stat);
            }
        }
        if (behaviors != null) {
            for (BehaviorEffect behavior : behaviors) {
                applyBehavior(plant, behavior);
            }
        }
    }

    public static void applyLevelBehaviors(Plant plant) {
        int levelIndex = plant.getLevel() - 2;
        List<BehaviorEffect>[] upgrades = plant.getBehaviorUpgrades();

        if (upgrades != null && levelIndex >= 0 && levelIndex < upgrades.length) {
            List<BehaviorEffect> currentLevelBehaviors = upgrades[levelIndex];
            if (currentLevelBehaviors != null) {
                for (BehaviorEffect effect : currentLevelBehaviors) {
                    applyBehavior(plant, effect);
                }
            }
        }
    }
}