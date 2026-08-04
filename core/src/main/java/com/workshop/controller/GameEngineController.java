package com.workshop.controller;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

public class GameEngineController {
    private static final double DELTA_TIME = 0.1;
    private com.workshop.controller.commandHandler.CommandRegistry registry;
    private boolean isRunning;
    private MenuManager mm;
    private DataManager dm;
    private GameContext gameContext;

    public GameEngineController() {
        this.mm = new MenuManager(null);
        this.dm = DataManager.getInstance();
        dm.loadUser();
        for (User u : UserManager.getInstance().users) {
            if (u.isStayedLogin()) {
                UserManager.getInstance().login(u);
                mm.forceChangeMenu("mainmenu");
                break;
            }
        }
        this.registry = new com.workshop.controller.commandHandler.CommandRegistry();
        com.workshop.controller.commandHandler.FileCommandProvider provider =
                new com.workshop.controller.commandHandler.FileCommandProvider(this.mm, gameContext);
        provider.registerCommands(this.registry);
    }


    public void start() {
        isRunning = true;
        Console.simplePrint("Game Started\n");
    }

    public void stop() {
        isRunning = false;
//        UserManager.getInstance().saveToFile();
        DataManager.getInstance().saveUser();
        Console.simplePrint("Saving data and exiting game...\n");
    }

    public void loop() {
        while (isRunning) {
            processInput();
            //update(); فقط با advance time باید بره جلو بازی
        }
    }

    public void processInput() {
        if (com.workshop.view.Console.hasNextLine()) {
            String input = com.workshop.view.Console.nextLine();
            if (input.isEmpty()) return;
            try {
                if (input.trim().equalsIgnoreCase("exit")) {
                    stop();
                    return;
                }
                registry.handleCommand(input);
                System.out.print("> ");
            } catch (Exception e) {
                com.workshop.view.Console.showMessage(e.getMessage());
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

