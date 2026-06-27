package controller;

import controller.repository.DataManager;
import model.TimeManager;

public class GameEngine {
    private controller.commandHandler.CommandRegistry registry;
    private boolean isRunning;
    private MenuManager mm;
    private DataManager dm;
    private TimeManager tm;

    public GameEngine(){
        this.mm = new MenuManager();
        this.dm = new DataManager();
        this.tm = new TimeManager();
        this.registry = new controller.commandHandler.CommandRegistry();
        controller.commandHandler.FileCommandProvider provider = new controller.commandHandler.FileCommandProvider(this.mm);
        provider.registerCommands(this.registry);
    }

    public void start(){
        isRunning = true;
        view.ConsoleView.simplePrint("Game Started");
    };
    public void stop(){
    };
    public void loop(){
        while (isRunning){
            processInput();
            update();
        }
    };
    public void processInput(){
        if (view.ConsoleView.hasNextLine()){
            String input = view.ConsoleView.nextLine();

            if (input.isEmpty()) {
                return;
            }

            try {
                registry.handleCommand(input);
            }catch (Exception e){
                view.ConsoleView.showMessage(e.getMessage());
            }
        }
    }; //process commands and pass to commandHandler
    public void update(){}; //calls every one to update
}
