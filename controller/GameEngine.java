package controller;

import controller.repository.DataManager;
import model.GameContext;
import model.TimeManager;
import model.level.Level;

public class GameEngine {
    private controller.commandHandler.CommandRegistry registry;
    private boolean isRunning;
    private MenuManager mm;
    private DataManager dm;
    private TimeManager tm;
    private GameContext ctx;
    private Level level;

    public GameEngine(){
        this.level = new Level();
        this.ctx = new GameContext(level);
        this.mm = new MenuManager(ctx);
        mm.changeMenu("registermenu");
        this.dm = new DataManager();
        this.tm = new TimeManager();
        this.registry = new controller.commandHandler.CommandRegistry();
        controller.commandHandler.FileCommandProvider provider = new controller.commandHandler.FileCommandProvider(this.mm);
        provider.registerCommands(this.registry);
    }

    public void start(){
        isRunning = true;
        view.ConsoleView.simplePrint("Game Started\n");
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
