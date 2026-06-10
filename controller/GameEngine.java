package controller;

import controller.repository.DataManager;
import model.TimeManager;

public class GameEngine {
    private boolean isRunning;
    private MenuManager mm;
    private DataManager dm;
    private TimeManager tm;

    public void start(){};
    public void stop(){};
    public void loop(){};
    public void processInput(){}; //process commands and pass to commandHandler
    public void update(){}; //calls every one to update
}
