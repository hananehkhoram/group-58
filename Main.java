import controller.GameEngine;
import controller.MenuManager;
import controller.commandHandler.CommandRegistry;
import controller.commandHandler.FileCommandProvider;
import controller.repository.DataManager;
import model.menus.allmenus.RegisterMenu;

import java.util.Scanner;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        GameEngine engine = new GameEngine();
        engine.start();
        engine.loop();
        MenuManager menuManager = new MenuManager();
        menuManager.changeMenu("RegisterMenu");
    }}
