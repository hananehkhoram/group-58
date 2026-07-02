package controller.repository;

public class DataManager {
    private static DataManager instance;
    public PlantRepository plants = new PlantRepository(); //
    public ZombieRepository zombies = new ZombieRepository();
    public UserRepository game = new UserRepository();

    private DataManager(){
        initialize();
    }
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    public void initialize(){
        plants.load("/home/hananehkhoram/aUni/AP/pvz/ap-project/Files/plants.csv");
//        zombies.load("pathtofile");
//        game.load("pathtofile");
    }
}
