package controller;

import controller.repository.DataManager;
import model.GameContext;
import model.TimeManager;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.season.Season;
import model.user.UserManager;
import view.ConsoleView;

public class GameEngineController {
    private static final double DELTA_TIME = 0.05; // 20 TPS
    private controller.commandHandler.CommandRegistry registry;
    private boolean isRunning;
    private MenuManager mm;
    private DataManager dm;

    public GameEngineController() {
        this.mm = new MenuManager(null);
        this.dm = DataManager.getInstance();
        dm.loadUser();
        this.registry = new controller.commandHandler.CommandRegistry();
        controller.commandHandler.FileCommandProvider provider =
                new controller.commandHandler.FileCommandProvider(this.mm);
        provider.registerCommands(this.registry);
    }


    public void start() {
        isRunning = true;
        ConsoleView.simplePrint("Game Started\n");
    }

    public void stop() {
        isRunning = false;
        UserManager.getInstance().saveToFile();
        DataManager.getInstance().saveUser();
        ConsoleView.simplePrint("Saving data and exiting game...\n");
    }

    public void loop() {
        while (isRunning) {
            processInput();
            update();
        }
    }

    public void processInput() {
        if (view.ConsoleView.hasNextLine()) {
            String input = view.ConsoleView.nextLine();
            if (input.isEmpty()) return;
            try {
                if (input.trim().equalsIgnoreCase("exit")) {
                    stop();
                    return;
                }
                registry.handleCommand(input);
                System.out.print("> ");
            } catch (Exception e) {
                view.ConsoleView.showMessage(e.getMessage());
            }
        }
    }

    public void update() {
        GameEngine engine = mm.getGameEngine();
        if (engine != null) {
            engine.update(DELTA_TIME);
        }
    }


}