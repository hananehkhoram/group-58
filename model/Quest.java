package model;

import java.util.ArrayList;
import model.plants.PlantFamily;

public class Quest {

    protected String id;
    protected String condition;
    public enum QuestLevel { LOW, MEDIUM, HIGH, CRITICAL }
    public enum QuestType { DAILY, MAIN, EPIC_CHALLENGE }

    protected int gemReward;
    protected int coinReward;

    protected int sun_amount;
    protected int questNumber; //progress

    ArrayList<String> season = new ArrayList<>();
    ArrayList<String> killerPlant = new ArrayList<>();
    public int[] n_seed = {0 , 1 , 2 , 3 , 4 , 5};
    public int[] n_Gem = {10 , 20 , 30 , 40 , 50};

    protected int numberOFrow;
    protected int nuimberOFcolumns;
    protected int min_row_column;

    public void specialreward(){}

}
