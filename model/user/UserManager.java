package model.user;

import java.util.List;

public class UserManager {//singelton

    private List<User> users;
    private User currentUser;

    private UserManager() {
    }

    public UserManager getInstance() {
        return null;
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
        return null;
    }

    public void saveToFile() {
    }

    public void loadFromFile() {
    }
}
