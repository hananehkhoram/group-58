package com.workshop.controller.repository;

import com.workshop.model.GreenHouseData.Pot;
import com.workshop.model.plants.Plant;
import com.workshop.model.shopData.DailyOffer;
import com.workshop.model.user.Gender;
import com.workshop.model.user.SecurityQuestions;
import com.workshop.model.user.User;
import com.workshop.model.zombie.Zombie;

import java.util.*;

public class UserRepository implements AssetRepository<User> {
    private final Map<String, User> userMap = new HashMap<>();
    private static final String FIELD_SEP = "|";
    private static final String LIST_SEP = ",";
    private static final String PLANT_SEP = "#";
    private static String PATH;

    public Map<String, User> getUserMap() {
        return userMap;
    }

    @Override
    public void load(String filePath) {
        java.nio.file.Path path = java.nio.file.Path.of(filePath);
        PATH = path.toString();

        if (!java.nio.file.Files.exists(path)) {
            return;
        }

        userMap.clear();

        try (java.io.BufferedReader reader =
                 java.nio.file.Files.newBufferedReader(
                     path,
                     java.nio.charset.StandardCharsets.UTF_8
                 )) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                User user = deserializeUser(line);
                userMap.put(user.getUsername(), user);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(
                "Could not load users from file: "
                    + path.toAbsolutePath(),
                e
            );
        }
    }

    public void save() {
        if (PATH == null || PATH.isBlank()) {
            throw new IllegalStateException(
                "User file path has not been initialized"
            );
        }

        java.nio.file.Path filePath = java.nio.file.Path.of(PATH);
        java.nio.file.Path parent = filePath.getParent();

        try {
            if (parent != null) {
                java.nio.file.Files.createDirectories(parent);
            }

            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                java.nio.file.Files.newBufferedWriter(
                    filePath,
                    java.nio.charset.StandardCharsets.UTF_8
                )
            )) {
                for (User user : userMap.values()) {
                    writer.println(serializeUser(user));
                }
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(
                "Could not save users to file: "
                    + filePath.toAbsolutePath(),
                e
            );
        }
    }

    private String serializeUser(User u) {
        String plantNames = u.getUnlockedPlantTypes() == null ? "" :
            String.join(LIST_SEP, u.getUnlockedPlantTypes().stream()
                .map(p -> p.getName() + PLANT_SEP + p.getLevel() + PLANT_SEP + p.isPlantFoodActive())
                .toList());
        String zombieNames = u.getSeenZombies() == null ? "" :
            String.join(LIST_SEP, u.getSeenZombies().stream().map(Zombie::getName).toList());
        String levels = u.getUnlockedLevels() == null ? "" : String.join(LIST_SEP, u.getUnlockedLevels());
        String completedQuests = u.getCompletedQuestIds() == null ? "" : String.join(LIST_SEP, u.getCompletedQuestIds());

        String plantName = (u.getLastDailyOffer() != null && u.getLastDailyOffer().getPlantType() != null)
            ? u.getLastDailyOffer().getPlantType().getName() : "";
        String dailyOffer = u.getLastDailyOffer() == null ? "" :
            u.getLastDailyOffer().getId() + LIST_SEP + u.getLastDailyOffer().getDate() + LIST_SEP
                + u.getLastDailyOffer().isPurchased() + LIST_SEP + plantName + LIST_SEP + u.isBoughtDailyOfferToday();

        String seedPackets = String.join(LIST_SEP,
            u.getPlantSeedsInventory().entrySet().stream()
                .map(e -> e.getKey() + PLANT_SEP + e.getValue())
                .toList());

        String storedBoostsStr = String.join(LIST_SEP,
            u.getStoredBoosts().entrySet().stream()
                .filter(java.util.Map.Entry::getValue)
                .map(java.util.Map.Entry::getKey)
                .toList());

        StringBuilder greenhouseSb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                Pot pot = u.getGreenHouse().getPot(i, j);
                greenhouseSb.append(pot.isLocked()).append(PLANT_SEP)
                    .append(pot.isEmpty()).append(PLANT_SEP)
                    .append(pot.getPlantType() == null ? "" : pot.getPlantType().getName()).append(PLANT_SEP)
                    .append(pot.getRemainingPlantedTime()).append(PLANT_SEP)
                    .append(pot.isPlantReady()).append(PLANT_SEP)
                    .append(pot.isMarigold());
                greenhouseSb.append(LIST_SEP);
            }
        }

        String questProgressStr = u.getAllQuestProgress() == null ? "" :
            String.join(LIST_SEP, u.getAllQuestProgress().entrySet().stream()
                .map(e -> e.getKey() + PLANT_SEP + e.getValue())
                .toList());

        return String.join(FIELD_SEP,
            u.getUsername(),
            u.getPassword(),
            u.getNickName(),
            u.getEmail(),
            u.getGender().name(),
            u.getSecurityQuestion() == null ? "" : String.valueOf(u.getSecurityQuestion().getId()),
            u.getSecurityAnswer() == null ? "" : u.getSecurityAnswer(),
            String.valueOf(u.isStayedLogin()),
            String.valueOf(u.getCoins()),
            String.valueOf(u.getGems()),
            String.valueOf(u.getDifficultyLevel()),
            String.valueOf(u.getLastReadNewsId()),
            String.valueOf(u.getGamesPlayed()),
            String.valueOf(u.getMaxMewPoint()),
            String.valueOf(u.getNumberOfPassedLevels()),
            String.valueOf(u.getPlantFoodCount()),
            String.valueOf(u.getOwnedPotsCount()),
            String.valueOf(u.getLastLevel()),
            String.valueOf(u.getLastSeason()),
            String.valueOf(u.getMinigamesCompleted()),
            String.valueOf(u.getDailyQuestsCompletedCount()),
            String.valueOf(u.getOtherQuestsCompletedCount()),
            levels,
            plantNames,
            zombieNames,
            completedQuests,
            seedPackets,
            greenhouseSb.toString(),
            dailyOffer,
            String.valueOf(u.getWinStreakAtMaxDifficulty()),
            questProgressStr,
            storedBoostsStr,
            String.valueOf(u.getGameSpeed()),
            String.valueOf(u.isGridEnabled()),
            String.valueOf(u.isDebugMode())
        );
    }

    private User deserializeUser(String line) {
        String[] f = line.split("\\" + FIELD_SEP, -1);
        User u = new User();

        u.setUsername(f[0]);
        u.setPassword(f[1]);
        u.setNickName(f[2]);
        u.setEmail(f[3]);
        u.setGender(Gender.valueOf(f[4]));
        if (!f[5].isBlank()) {
            u.setSecurityQuestion(SecurityQuestions.getQuestionById(Integer.parseInt(f[5])));
            u.setSecurityAnswer(f[6]);
        }
        u.setStayedLogin(Boolean.parseBoolean(f[7]));
        u.setCoins(Integer.parseInt(f[8]));
        u.setGems(Integer.parseInt(f[9]));
        u.setDifficultyLevel(Integer.parseInt(f[10]));
        u.setLastReadNewsId(Integer.parseInt(f[11]));
        u.setGamesPlayed(Integer.parseInt(f[12]));
        u.setMaxMewPoint(Integer.parseInt(f[13]));
        u.setNumberOfPassedLevels(Integer.parseInt(f[14]));
        u.setPlantFoodCount(Integer.parseInt(f[15]));
        u.setOwnedPotsCount(Integer.parseInt(f[16]));
        u.setLastLevel(Integer.parseInt(f[17]));
        u.setLastSeason(Integer.parseInt(f[18]));
        u.setMinigamesCompleted(Integer.parseInt(f[19]));
        u.setDailyQuestsCompletedCount(Integer.parseInt(f[20]));
        u.setOtherQuestsCompletedCount(Integer.parseInt(f[21]));

        ArrayList<String> levels = new ArrayList<>();
        if (!f[22].isBlank()) {
            levels.addAll(List.of(f[22].split(LIST_SEP)));
        }
        u.setUnlockedLevels(levels);

        List<Plant> plants = new ArrayList<>();
        if (!f[23].isBlank()) {
            for (String entry : f[23].split(LIST_SEP)) {
                String[] parts = entry.split(PLANT_SEP, 3);
                String plantName = parts[0];
                int plantLevel = parts.length > 1 && !parts[1].isBlank() ? Integer.parseInt(parts[1]) : 1;
                boolean isBoosted = parts.length > 2 && Boolean.parseBoolean(parts[2]);
                Plant p = DataManager.getInstance().plants.get(plantName);
                if (p != null) {
                    p.setLevel(plantLevel);
                    p.setPlantFoodActive(isBoosted);
                    plants.add(p);
                }
            }
        }
        u.setUnlockedPlantTypes(plants);

        List<Zombie> zombies = new ArrayList<>();
        if (!f[24].isBlank()) {
            for (String name : f[24].split(LIST_SEP)) {
                Zombie z = DataManager.getInstance().zombies.get(name);
                if (z != null) zombies.add(z);
            }
        }
        u.setSeenZombies(zombies);

        if (!f[25].isBlank()) {
            u.setCompletedQuestIds(new HashSet<>(List.of(f[25].split(LIST_SEP))));
        }

        if (!f[26].isBlank()) {
            for (String entry : f[26].split(LIST_SEP)) {
                String[] parts = entry.split(PLANT_SEP, 2);
                u.addSeedsToInventory(parts[0], Integer.parseInt(parts[1]));
            }
        }

        if (!f[27].isBlank()) {
            String[] potEntries = f[27].split(LIST_SEP);
            int idx = 0;
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 5; j++) {
                    String[] parts = potEntries[idx++].split(PLANT_SEP, -1);
                    Pot pot = u.getGreenHouse().getPot(i, j);
                    pot.setLocked(Boolean.parseBoolean(parts[0]));
                    pot.setEmpty(Boolean.parseBoolean(parts[1]));
                    if (!parts[2].isBlank()) {
                        pot.setPlantType(DataManager.getInstance().plants.get(parts[2]));
                    }
                    pot.setRemainingPlantedTime(Double.parseDouble(parts[3]));
                    pot.setPlantReady(Boolean.parseBoolean(parts[4]));
                    if (parts.length > 5 && !parts[5].isBlank()) {
                        pot.setMarigold(Boolean.parseBoolean(parts[5]));
                    }
                }
            }
        }

        if (f.length > 28 && !f[28].isBlank()) {
            String[] offerParts = f[28].split(LIST_SEP, -1);
            DailyOffer d = new DailyOffer(Integer.parseInt(offerParts[0]), Long.parseLong(offerParts[1]), Boolean.parseBoolean(offerParts[2]));
            if (offerParts.length > 3 && !offerParts[3].isBlank()) {
                Plant p = DataManager.getInstance().plants.get(offerParts[3]);
                d.setPlantType(p);
            }
            u.setLastDailyOffer(d);
            if (offerParts.length > 4) {
                u.setBoughtDailyOfferToday(Boolean.parseBoolean(offerParts[4]));
            }
        }

        if (f.length > 29 && !f[29].isBlank()) {
            u.setWinStreakAtMaxDifficulty(Integer.parseInt(f[29]));
        }

        if (f.length > 30 && !f[30].isBlank()) {
            Map<String, Integer> progress = new HashMap<>();
            for (String entry : f[30].split(LIST_SEP)) {
                String[] parts = entry.split(PLANT_SEP, 2);
                if (parts.length == 2) {
                    progress.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
            u.setAllQuestProgress(progress);
        }
        if (f.length > 31 && !f[31].isBlank()) {
            for (String plantName : f[31].split(LIST_SEP)) {
                u.addStoredBoost(plantName);
            }
        }

        if (f.length > 32 && !f[32].isBlank()) {
            u.setGameSpeed(Integer.parseInt(f[32]));
        }

        if (f.length > 33 && !f[33].isBlank()) {
            u.setGridEnabled(Boolean.parseBoolean(f[33]));
        }

        if (f.length > 34 && !f[34].isBlank()) {
            u.setDebugMode(Boolean.parseBoolean(f[34]));
        }

        return u;
    }

    @Override
    public User get(String id) {
        return userMap.get(id);
    }
}
