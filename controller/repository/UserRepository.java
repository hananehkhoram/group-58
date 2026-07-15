package controller.repository;

import model.plants.Plant;
import model.shopData.DailyOffer;
import model.user.Gender;
import model.user.SecurityQuestions;
import model.user.User;
import model.user.UserManager;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            for (User user : UserManager.getInstance().users){
                writer.println(serializeUser(user));
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
                String.join(LIST_SEP, u.getSeenZombies().stream().map(model.zombie.Zombie::getName).toList());
        String levels = u.getUnlockedLevels() == null ? "" : String.join(LIST_SEP, u.getUnlockedLevels());


        String dailyOffers = u.getLastDailyOffer() == null ? "" : u.getLastDailyOffer().getId() + LIST_SEP + u.getLastDailyOffer().getDate() + LIST_SEP + u.getLastDailyOffer().isPurchased();
        String dailyOfferId = u.getLastDailyOffer() == null ? "-1" : String.valueOf(u.getLastDailyOffer().getId());

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
                dailyOfferId,
                String.valueOf(u.getGamesPlayed()),
                String.valueOf(u.getMaxMewPoint()),
                String.valueOf(u.getNumberOfPassedLevels()),
                String.valueOf(u.getPlantFoodCount()),
                String.valueOf(u.getOwnedPotsCount()),
                levels,
                plantNames,
                zombieNames,
                dailyOffers
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

        u.setGamesPlayed(Integer.parseInt(f[13]));
        u.setMaxMewPoint(Integer.parseInt(f[14]));
        u.setNumberOfPassedLevels(Integer.parseInt(f[15]));
        u.setPlantFoodCount(Integer.parseInt(f[16]));
        u.setOwnedPotsCount(Integer.parseInt(f[17]));

        ArrayList<String> levels = new ArrayList<>();
        if (!f[18].isBlank()) {
            levels.addAll(java.util.List.of(f[18].split(LIST_SEP)));
        }
        u.setUnlockedLevels(levels);

        List<Plant> plants = new ArrayList<>();
        if (!f[19].isBlank()) {
            for (String entry : f[19].split(LIST_SEP)) {
                String[] parts = entry.split(PLANT_SEP, 2);
                String plantName = parts[0];
                int plantLevel = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                Plant p = controller.repository.DataManager.getInstance().plants.get(plantName);
                if (p != null) {
                    p.setLevel(plantLevel);
                    plants.add(p);
                }
            }
        }
        u.setUnlockedPlantTypes(plants);

        List<Zombie> zombies = new ArrayList<>();
        if (!f[20].isBlank()) {
            for (String name : f[20].split(LIST_SEP)) {
                Zombie z = controller.repository.DataManager.getInstance().zombies.get(name);
                if (z != null) {
                    zombies.add(z);
                }
            }
        }
        u.setSeenZombies(zombies);

        if (!f[21].isBlank()) {
            String[] offerParts = f[21].split(LIST_SEP);
            if (offerParts.length >= 3) {
                int offerId = Integer.parseInt(offerParts[0]);
                long date = Long.parseLong(offerParts[1]);
                boolean isPurchased = Boolean.parseBoolean(offerParts[2]);
                DailyOffer d = new DailyOffer(offerId, date, isPurchased);
                u.setLastDailyOffer(d);
            }
        }
        return u;
    }


    @Override
    public User get(String id) {
        return null;
    }
}
