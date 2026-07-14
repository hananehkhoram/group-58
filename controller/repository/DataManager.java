package controller.repository;

public class DataManager {
    private static DataManager instance;
    public PlantRepository plants = new PlantRepository(); //
    public ZombieRepository zombies = new ZombieRepository();
    public UserRepository game = new UserRepository();
    public SeasonRepository seasons = new SeasonRepository();

    private DataManager(){
        instance = this;
        initialize();
    }
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    public void initialize(){
        plants.load("Files/plants.csv");
//        zombies.load("pathtofile");
//        game.load("pathtofile");
        seasons.load(null);
    }
}
