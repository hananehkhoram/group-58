package controller.repository;

public class DataManager {
    public PlantRepository plants = new PlantRepository(); //
    public ZombieRepository zombies = new ZombieRepository();
    public UserRepository game = new UserRepository();
    public void initialize(){
        plants.load("/home/hananehkhoram/aUni/AP/pvz/ap-project/Files/plants.csv");
//        zombies.load("pathtofile");
//        game.load("pathtofile");
    }
}
