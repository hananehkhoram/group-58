package controller.commands.Status;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.plants.Plant;
import view.ConsoleView;

public class ShowPlantsStatus implements Command {
    private MenuManager menuManager;

    public ShowPlantsStatus(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("====Plants status====\n");
        GameContext ctx = menuManager.getCtx();
        for (Plant p : ctx.getActivePlants()){
            boolean plantable = !ctx.isOnCooldown(p.getName());
            sb.append(p.getName()).append(" - Sun cost: ").append(p.getSunCost());

            if (plantable) {
                sb.append(" - Plantable: Yes\n");
            } else {
                double remaining = ctx.getRemainingCooldownSeconds(p.getName());
                sb.append(" - Plantable: No (ready in ").append(remaining).append("s)\n");
            }
        }
        ConsoleView.showMessage(sb.toString());
    }

    //show plants status
}
