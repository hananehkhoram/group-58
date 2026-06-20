package model.user;

public enum SecurityQuestions {
    FATHER_NAME(1, "What is your father's name?"),
    FIRST_PET(2, "What was the name of your first pet?"),
    BIRTH_CITY(3, "What city were you born in?");

    private final int id;
    private final String questionText;

    SecurityQuestions(int id, String questionText) {
        this.id = id;
        this.questionText = questionText;
    }

    public int getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public static SecurityQuestions getQuestionById(int id) {
        for (SecurityQuestions question : SecurityQuestions.values()) {
            if (question.getId() == id) {
                return question;
            }
        }
        return null;
    }
}