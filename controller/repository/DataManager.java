package controller.repository;

public class DataManager {
    public PlantRepository plants = new PlantRepository(); //
    public ZombieRepository zombies = new ZombieRepository();
    public UserRepository game = new UserRepository();
    public void initialize(){
        plants.load("pathtofile");
        zombies.load("pathtofile");
        game.load("pathtofile");
    }
}
