package controller.repository;

import model.GreenHouseData.Pot;
import model.plants.Plant;
import model.shopData.DailyOffer;
import model.user.Gender;
import model.user.SecurityQuestions;
import model.user.User;
import model.user.UserManager;
import model.zombie.Zombie;

import java.util.*;

// load and save data
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
        java.io.File file = new java.io.File(filePath);
        PATH = file.getPath();
        if (!file.exists()) return;

        userMap.clear();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                User u = deserializeUser(line);
                userMap.put(u.getUsername(), u);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not load users from file", e);
        }

    }

    public void save() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.FileWriter(PATH, java.nio.charset.StandardCharsets.UTF_8))) {
            for (User u : userMap.values()) {
                writer.println(serializeUser(u));
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not save users to file", e);
        }
    }


    private String serializeUser(User u) {
        String plantNames = u.getUnlockedPlantTypes() == null ? "" :
                String.join(LIST_SEP, u.getUnlockedPlantTypes().stream()
                                      .map(p -> p.getName() + PLANT_SEP + p.getLevel())
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

        StringBuilder greenhouseSb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                Pot pot = u.getGreenHouse().getPot(i, j);
                greenhouseSb.append(pot.isLocked()).append(PLANT_SEP)
                        .append(pot.isEmpty()).append(PLANT_SEP)
                        .append(pot.getPlantType() == null ? "" : pot.getPlantType().getName()).append(PLANT_SEP)
                        .append(pot.getRemainingPlantedTime()).append(PLANT_SEP)
                        .append(pot.isPlantReady());
                greenhouseSb.append(LIST_SEP);
            }
        }

        return String.join(FIELD_SEP,
                u.getUsername(),                                              // 0
                u.getPassword(),                                              // 1
                u.getNickName(),                                              // 2
                u.getEmail(),                                                 // 3
                u.getGender().name(),                                         // 4
                u.getSecurityQuestion() == null ? "" : String.valueOf(u.getSecurityQuestion().getId()), // 5
                u.getSecurityAnswer() == null ? "" : u.getSecurityAnswer(),   // 6
                String.valueOf(u.isStayedLogin()),                            // 7
                String.valueOf(u.getCoins()),                                 // 8
                String.valueOf(u.getGems()),                                  // 9
                String.valueOf(u.getDifficultyLevel()),                       // 10
                String.valueOf(u.getLastReadNewsId()),                        // 11
                String.valueOf(u.getGamesPlayed()),                           // 12
                String.valueOf(u.getMaxMewPoint()),                           // 13
                String.valueOf(u.getNumberOfPassedLevels()),                  // 14
                String.valueOf(u.getPlantFoodCount()),                        // 15
                String.valueOf(u.getOwnedPotsCount()),                        // 16
                String.valueOf(u.getLastLevel()),                             // 17
                String.valueOf(u.getLastSeason()),                            // 18
                String.valueOf(u.getMinigamesCompleted()),                    // 19
                String.valueOf(u.getDailyQuestsCompletedCount()),             // 20
                String.valueOf(u.getOtherQuestsCompletedCount()),             // 21
                levels,                                                       // 22
                plantNames,                                                   // 23
                zombieNames,                                                  // 24
                completedQuests,                                              // 25
                seedPackets,                                                  // 26
                greenhouseSb.toString(),                                      // 27
                dailyOffer                                                    // 28
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
                String[] parts = entry.split(PLANT_SEP, 2);
                String plantName = parts[0];
                int plantLevel = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                Plant p = DataManager.getInstance().plants.get(plantName);
                if (p != null) {
                    p.setLevel(plantLevel);
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
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 4; j++) {
                    String[] parts = potEntries[idx++].split(PLANT_SEP, -1);
                    Pot pot = u.getGreenHouse().getPot(i, j);
                    pot.setLocked(Boolean.parseBoolean(parts[0]));
                    pot.setEmpty(Boolean.parseBoolean(parts[1]));
                    if (!parts[2].isBlank()) {
                        pot.setPlantType(DataManager.getInstance().plants.get(parts[2]));
                    }
                    pot.setRemainingPlantedTime(Double.parseDouble(parts[3]));
                    pot.setPlantReady(Boolean.parseBoolean(parts[4]));
                }
            }
        }

        if (f.length > 28 && !f[28].isBlank()) {
            String[] offerParts = f[28].split(LIST_SEP, -1);
            DailyOffer d = new DailyOffer(Integer.parseInt(offerParts[0]), Long.parseLong(offerParts[1]), Boolean.parseBoolean(offerParts[2]));
            u.setLastDailyOffer(d);
            if (offerParts.length > 3 && !offerParts[3].isBlank()) {
                Plant p = DataManager.getInstance().plants.get(offerParts[3]);
                u.setLastDailyOfferPlant(p);
            }
            if (offerParts.length > 4) {
                u.setBoughtDailyOfferToday(Boolean.parseBoolean(offerParts[4]));
            }
        }

        return u;
    }


    @Override
    public User get(String id) {
        return null;
    }
}
