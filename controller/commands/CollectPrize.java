package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.mechanisms.LootItem;
import model.user.User;
import model.user.UserManager;
import view.ConsoleView;

import java.util.Iterator;

public class CollectPrize implements Command {
    private MenuManager menuManager;

    public CollectPrize(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        GameContext ctx = menuManager.getCtx();
        if (ctx == null) {
            ConsoleView.showMessage("No active battle.");
            return;
        }

        User user = UserManager.getInstance().getCurrentUser();
        if (user == null) return;

        if (ctx.getActiveLoots().isEmpty()) {
            ConsoleView.showMessage("There are no prizes on the ground to collect!");
            return;
        }

        Iterator<LootItem> it = ctx.getActiveLoots().iterator();
        while (it.hasNext()) {
            LootItem loot = it.next();
            switch (loot.getType()) {
                case COIN -> {
                    user.setCoins(user.getCoins() + 50);
                    ConsoleView.showMessage("Collected 50 coins! Total coins: " + user.getCoins());
                }
                case DIAMOND -> {
                    user.setGems(user.getGems() + 1);
                    ConsoleView.showMessage("Collected 1 diamond! Total diamonds: " + user.getGems());
                }
                case POT -> {
                    user.setOwnedPotsCount(user.getOwnedPotsCount() + 1);
                    ConsoleView.showMessage("Collected 1 pot! Total pots: " + user.getOwnedPotsCount());
                }
            }
            it.remove();
        }
    }
}