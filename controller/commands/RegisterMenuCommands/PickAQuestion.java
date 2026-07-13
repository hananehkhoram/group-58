package controller.commands.RegisterMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import exceptions.CommandNotFound;
import model.menus.Menu;
import model.menus.allmenus.RegisterMenu;
import view.ConsoleView;

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
            ConsoleView.showMessage("%s\n",result);
            if (result.startsWith("Registered")){
                ConsoleView.showMessage("You can now enter the Login menu.");
            }

        }
    }

    //pick question -q <question_number> -a <answer> -c <answer_confirm> p_11
}
