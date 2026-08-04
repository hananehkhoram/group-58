package com.workshop.controller.commands.RegisterMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.exceptions.CommandNotFound;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.RegisterMenu;
import com.workshop.view.Console;
public class PickAQuestion implements Command {
    private MenuManager menuManager;

    public PickAQuestion(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String qNumberStr = args[0];
        int qNumber;
        try {
            qNumber = Integer.parseInt(qNumberStr);
        } catch (NumberFormatException e) {
            throw new CommandNotFound("Invalid question number!\n");
        }
        String answer = args[1];
        String answerConfirm = args[2];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof RegisterMenu){
            String result = ((RegisterMenu) currentMenu).pickQuestion(qNumber,answer,answerConfirm);
            Console.showMessage("%s\n",result);
            if (result.startsWith("Registered")){
                Console.showMessage("You can now enter the Login menu.");
            }

        }
    }

    //pick question -q <question_number> -a <answer> -c <answer_confirm> p_11
}
