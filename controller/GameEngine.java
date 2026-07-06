package controller;

import controller.repository.DataManager;
import model.GameContext;
import model.TimeManager;
import model.level.Level;
import model.season.AncientEgypt;
import model.season.Season;
import view.ConsoleView;

public class GameEngine {
    private controller.commandHandler.CommandRegistry registry;
    private boolean isRunning;
    private MenuManager mm;
    private DataManager dm;
    private TimeManager tm;
    private GameContext ctx;
    private Season season;
    private Level level;

    public GameEngine(){
        this.mm = new MenuManager(null);
        mm.changeMenu("registermenu");
        this.dm = DataManager.getInstance();
        this.tm = new TimeManager();
        this.registry = new controller.commandHandler.CommandRegistry();
        controller.commandHandler.FileCommandProvider provider = new controller.commandHandler.FileCommandProvider(this.mm);
        provider.registerCommands(this.registry);
    }

    public void start(){
        isRunning = true;
        ConsoleView.simplePrint("Game Started\n");
    };
    public void stop(){
        isRunning = false;
        ConsoleView.simplePrint("Saving data and exiting game...\n");
        //save
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
                if (input.trim().equalsIgnoreCase("exit")) {
                    stop();
                    return;
                }
                registry.handleCommand(input);
            }catch (Exception e){
                view.ConsoleView.showMessage(e.getMessage());
            }
        }
    }; //process commands and pass to commandHandler
    public void update(){
        //ctx.update(tm.getTotalTicks());
    }; //calls every one to update
}
