package model.plants.upgradeEffect;

import model.plants.Plant;

import java.util.List;

public class UpgradeManager {

    /*
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
                // افزایش جان پایه (و جان فعلی تا گیاه بلافاصله جون بگیره)
                plant.setBaseHp((int) (plant.getBaseHp() + amount));
                plant.heal((int) amount);
                break;

            case DMG:
                // چون در کدهای قبلی (کلاس Lobber) دمیج از نوع String بود، اینجا هم تبدیل می‌کنیم
                try {
                    int currentDmg = Integer.parseInt(plant.getDamage());
                    plant.setDamage(String.valueOf(currentDmg + (int) amount));
                } catch (NumberFormatException e) {
                    plant.setDamage(String.valueOf((int) amount)); // اگر دمیج از قبل مقدار نداشت
                }
                break;

            case COOLDOWN:
                // تغییر زمان ساخت مجدد (اگر amount منفی باشد، زمان را کم می‌کند)
                double newCooldown = plant.getCooldown() + amount;
                if (newCooldown < 0) newCooldown = 0; // زمان کول‌داون نمی‌تونه منفی بشه
                plant.setCooldown(newCooldown);
                break;

            case COST:
                // تغییر هزینه خورشید
                int newCost = plant.getSunCost() + (int) amount;
                if (newCost < 0) newCost = 0;
                plant.setSunCost(newCost);
                break;

            case ATK_SPEED:
                // اگر سرعت حمله را با یک فیلد String به نام interval ذخیره می‌کنی
                // باید مقدار interval کاهش پیدا کند تا گیاه تندتر شلیک کند
                break;

            // TODO: بقیه کیس‌ها (مثل RANGE، SUN_DROP و ...) را می‌توانی بر اساس فیلدهای کلاس Plant اینجا اضافه کنی

            default:
                // برای جلوگیری از گم شدن آپگریدهایی که هنوز پیاده‌سازی نشده‌اند
                System.out.println("[سیستم ارتقا]: ارتقای " + effect.getKey() + " هنوز روی کلاس Plant مپ نشده است.");
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
    }*/
}