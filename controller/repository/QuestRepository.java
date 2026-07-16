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
        add("sun-collector-3000", "آفتاب گیر روزانه", Quest.QuestCategory.DAILY,
                "جمع‌آوری ۳۰۰۰ خورشید در یک روز", Quest.RewardType.COIN, 30, Quest.QuestLevel.MEDIUM, 3000);
        add("sun-collector-4000", "آفتاب گیر روزانه", Quest.QuestCategory.DAILY,
                "جمع‌آوری ۴۰۰۰ خورشید در یک روز", Quest.RewardType.COIN, 40, Quest.QuestLevel.MEDIUM, 4000);
        add("sun-collector-5000", "آفتاب گیر روزانه", Quest.QuestCategory.DAILY,
                "جمع‌آوری ۵۰۰۰ خورشید در یک روز", Quest.RewardType.COIN, 50, Quest.QuestLevel.MEDIUM, 5000);

        for (Season s : DataManager.getInstance().seasons.getMainChapters()) {
            add("chapter-hunter-" + s.getName(), "شکارچی " + s.getName(), Quest.QuestCategory.MAIN,
                    "شکست دادن ۵۰ زامبی از فصل " + s.getName(), Quest.RewardType.SEED_PACKET, 10, Quest.QuestLevel.HIGH, 50);
        }

        add("plant-pro-killer", "plant باز حرفه‌ای", Quest.QuestCategory.DAILY,
                "ده‌تا زامبی را فقط با یک گیاه بکش", Quest.RewardType.RANDOM_PLANT, 1, Quest.QuestLevel.HIGH, 1);
        add("only-cactus", "only cactus", Quest.QuestCategory.DAILY,
                "ده‌تا زامبی را فقط با کاکتوس بکش", Quest.RewardType.GEM, 20, Quest.QuestLevel.HIGH, 1);

        for (int n = 0; n <= 5; n++) {
            add("economic-herbivore-" + n, "گیاه‌خوار اقتصادی", Quest.QuestCategory.MAIN,
                    "پیروزی بدون از دست دادن بیش از " + n + " گیاه",
                    Quest.RewardType.SEED_PACKET, 20 - n, Quest.QuestLevel.HIGH, 1);
        }

        add("defense-master", "استاد دفاع", Quest.QuestCategory.EPIC,
                "اتمام یک مرحله دقیقا با صفر خورشید", Quest.RewardType.GEM, 200, Quest.QuestLevel.CRITICAL, 1);
        add("quick-action", "سرعت عمل", Quest.QuestCategory.MAIN,
                "کشتن ۱۰ زامبی در کمتر از ۳۰ ثانیه از شروع موج اول", Quest.RewardType.COIN, 500, Quest.QuestLevel.MEDIUM, 1);
        add("pro-destroyer", "تخریب گر حرفه ای", Quest.QuestCategory.DAILY,
                "استفاده از ۳ گیاه انفجاری در یک مرحله", Quest.RewardType.COIN, 100, Quest.QuestLevel.LOW, 1);
        add("symmetry", "تقارن", Quest.QuestCategory.DAILY,
                "باغچه باید در پایان متقارن باشد", Quest.RewardType.COIN, 500, Quest.QuestLevel.HIGH, 1);

        for (PlantFamily f : PlantFamily.values()) {
            add("family-kill-" + f, "کشتار خانوادگی", Quest.QuestCategory.DAILY,
                    "تنها با خانواده‌ی " + f + " زامبی بکش", Quest.RewardType.COIN, 1000, Quest.QuestLevel.MEDIUM, 1);
            add("family-avoid-" + f, "شکوفایی در محدودیت‌ها", Quest.QuestCategory.DAILY,
                    "برد بدون استفاده از خانواده‌ی " + f, Quest.RewardType.GEM, 100, Quest.QuestLevel.HIGH, 1);
        }

        add("night-or-morning", "شب یا صبح", Quest.QuestCategory.EPIC,
                "پایان‌رساندن مرحله‌ی شب با گیاهان قارچی", Quest.RewardType.GEM, 20, Quest.QuestLevel.HIGH, 1);
        add("win-streak-5", "برد پشت برد", Quest.QuestCategory.DAILY,
                "۵ مرحله پشت‌سرهم با بیشترین سختی ببر", Quest.RewardType.COIN, 5000, Quest.QuestLevel.MEDIUM, 1);
        add("almost-lost", "تقریبا پیروز", Quest.QuestCategory.DAILY,
                "۱۰ زامبی در ستونِ اولِ ردیف بدون چمن‌زن بکش", Quest.RewardType.COIN, 300, Quest.QuestLevel.MEDIUM, 1);
        add("ocd", "OCD نمنه", Quest.QuestCategory.DAILY,
                "برد بدون تقارن در باغچه (بجز ردیف وسط)", Quest.RewardType.COIN, 800, Quest.QuestLevel.MEDIUM, 1);
        add("cloudy-day", "روز ابری", Quest.QuestCategory.DAILY,
                "برد فقط با ۳ گیاه تولیدکننده‌ی خورشید", Quest.RewardType.GEM, 10, Quest.QuestLevel.HIGH, 1);

        for (int col = 0; col < 9; col++) {
            add("one-less-column-" + col, "یه ستون کمتر", Quest.QuestCategory.DAILY,
                    "برد بدون کاشت در ستون " + col, Quest.RewardType.GEM, 10, Quest.QuestLevel.HIGH, 1);
        }
        for (int row = 0; row < 5; row++) {
            add("undefended-row-" + row, "سطر بی دفاع", Quest.QuestCategory.DAILY,
                    "برد بدون کاشت در سطر " + row, Quest.RewardType.GEM, 20, Quest.QuestLevel.HIGH, 1);
        }
        add("undefended-cross", "صلیب بی دفاع", Quest.QuestCategory.DAILY,
                "برد با سطر و ستون خالی مشخص", Quest.RewardType.GEM, 25, Quest.QuestLevel.HIGH, 1);

        for (int n : new int[]{10, 20, 30, 40, 50}) {
            add("lawnmower-time-" + n, "وقت چمن‌زنی", Quest.QuestCategory.EPIC,
                    "حداقل " + n + " زامبی با چمن‌زن بکش", Quest.RewardType.GEM, n, Quest.QuestLevel.MEDIUM, n);
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