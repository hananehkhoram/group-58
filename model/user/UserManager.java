package model.user;

import model.plants.Plant;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class UserManager {//singelton
    private static UserManager instance = null;

    private List<User> users;
    private User currentUser;

    private static final String USERS_FILE = "Files/users.dat";
    private static final String FIELD_SEP = "|";
    private static final String LIST_SEP = ",";

    private UserManager() {
        this.users = new ArrayList<>();
        this.currentUser = null;
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User findUserByName(String name){
        User foundUser = null;
        for (User u : users) {
            if (u.getUsername().equals(name)) {
                foundUser = u;
                break;
            }
        }
        return foundUser;
    }

    public boolean isUsernameValid(String username){
        if (username == null) {
            return false;
        }
        String regex = "^[a-zA-Z0-9-]+$";
        return username.matches(regex);
    }
    public boolean isPasswordValid(String password){
        if (password == null) {
            return false;
        }
        String regex = "^[a-zA-Z0-9\\p{Punct}]+$";
        return password.matches(regex);
    }
    public boolean doesUserExist(String username){
        for (User user : users){
            if (user.username.equals(username)) return true;
        }
        return false;
    }
    public String isPasswordStrong(String password){
        if (password.length() < 8) {
            return "Password is too short! It must be at least 8 characters.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit.";
        }
        String specialCharsRegex = ".*[!#\\$%\\^&\\*\\(\\)=\\+\\{\\}\\]\\[\\|/\\\\:;'\",<>\\?].*";
        if (!password.matches(specialCharsRegex)) {
            return "Password must contain at least one special character ( ! # $ % ^ & * ) ( = + { } ] [ | / \\ : ; ' \" , < > ?).";
        }

        return "ok";
    }
    public boolean doesPasswordsMatch(String password,String validation){
        return password.equals(validation);
    }
    public boolean isNickNameValid(String nickName){
        return nickName.length() >= 3 && nickName.length() <= 30;
    }
    public boolean isEmailValid(String email) {
        if (email == null) return false;
        String regex = "^[a-zA-Z0-9](?:[a-zA-Z0-9._-]*[a-zA-Z0-9])?@[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.[a-zA-Z]{2,}$";

        if (!email.matches(regex)) return false;

        return !email.contains("..");
    }

    public void register(String username, String password, String nickName,
                         String email,String gender){
        String hashedPassword = Security.hashPassword(password);
        currentUser = new User(username,hashedPassword,nickName,email,
                gender.equalsIgnoreCase("female") ? Gender.FEMALE : Gender.MALE);
        users.add(currentUser);
        saveToFile();
    }
    public void addQuestion(SecurityQuestions selectedQuestion,String answer){
        getCurrentUser().securityQuestion = selectedQuestion;
        getCurrentUser().securityAnswer = answer;
    }

    public boolean isAnswerCorrect(String answer,User user){
        if (user.getSecurityAnswer().equals(answer)) return true;
        return false;
    }

    public boolean isPasswordCorrect(String password,String username){
        User foundUser = findUserByName(username);
        String hashedEnteredPassword = Security.hashPassword(password);

        if (!foundUser.getPassword().equals(hashedEnteredPassword)) {
            return false;
        }
        return true;
    }

    public void changePassword(String password){
        currentUser.password = Security.hashPassword(password);
    }

    public boolean isEmailCorrect(String email,String name){
        User foundUser = findUserByName(name);
        if (!foundUser.getEmail().equalsIgnoreCase(email)) return false;
        return true;
    }


    public void login(User user) {
        this.currentUser = user;
    }

    public void logOut() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void saveToFile() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.FileWriter(USERS_FILE, java.nio.charset.StandardCharsets.UTF_8))) {
            for (User u : users) {
                writer.println(serializeUser(u));
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not save users to file", e);
        }
    }

    public void loadFromFile() {
        java.io.File file = new java.io.File(USERS_FILE);
        if (!file.exists()) return;

        users.clear();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                users.add(deserializeUser(line));
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not load users from file", e);
        }
    }
    private String serializeUser(User u) {
        String plantNames = u.getUnlockedPlantTypes() == null ? "" :
                String.join(LIST_SEP, u.getUnlockedPlantTypes().stream().map(model.plants.Plant::getName).toList());
        String zombieNames = u.getSeenZombies() == null ? "" :
                String.join(LIST_SEP, u.getSeenZombies().stream().map(model.zombie.Zombie::getName).toList());
        String levels = u.getUnlockedLevels() == null ? "" : String.join(LIST_SEP, u.getUnlockedLevels());

        return String.join(FIELD_SEP,
                u.getUsername(),
                u.getPassword(),
                u.getNickName(),
                u.getEmail(),
                u.getGender().name(),
                u.getSecurityQuestion() == null ? "" : String.valueOf(u.getSecurityQuestion().getId()),
                u.getSecurityAnswer() == null ? "" : u.getSecurityAnswer(),
                String.valueOf(u.getCoins()),
                String.valueOf(u.getGems()),
                String.valueOf(u.getDifficultyLevel()),
                String.valueOf(u.getLastReadNewsId()),
                String.valueOf(u.getGamesPlayed()),
                String.valueOf(u.getPlantFoodCount()),
                String.valueOf(u.getOwnedPotsCount()),
                levels,
                plantNames,
                zombieNames
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
            addQuestion(SecurityQuestions.getQuestionById(Integer.parseInt(f[5])), f[6]);
        }

        u.setCoins(Integer.parseInt(f[7]));
        u.setGems(Integer.parseInt(f[8]));
        u.setDifficultyLevel(Integer.parseInt(f[9]));
        u.setLastReadNewsId(Integer.parseInt(f[10]));
        u.setGamesPlayed(Integer.parseInt(f[11]));
        u.setPlantFoodCount(Integer.parseInt(f[12]));
        u.setOwnedPotsCount(Integer.parseInt(f[13]));

        ArrayList<String> levels = new ArrayList<>();
        if (!f[14].isBlank()) levels.addAll(java.util.List.of(f[14].split(LIST_SEP)));
        u.setUnlockedLevels(levels);

        List<Plant> plants = new ArrayList<>();
        if (!f[15].isBlank()) {
            for (String name : f[15].split(LIST_SEP)) {
                Plant p = controller.repository.DataManager.getInstance().plants.get(name);
                if (p != null) plants.add(p);
            }
        }
        u.setUnlockedPlantTypes(plants);

        List<Zombie> zombies = new ArrayList<>();
        if (!f[16].isBlank()) {
            for (String name : f[16].split(LIST_SEP)) {
                Zombie z = controller.repository.DataManager.getInstance().zombies.get(name);
                if (z != null) zombies.add(z);
            }
        }
        u.setSeenZombies(zombies);

        return u;
    }
    public void clearAllUsers() {
        users.clear();
        currentUser = null;
        java.io.File file = new java.io.File(USERS_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

}
