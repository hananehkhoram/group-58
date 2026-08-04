package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.LootItem;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

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
            Console.showMessage("No active battle.");
            return;
        }

        User user = UserManager.getInstance().getCurrentUser();
        if (user == null) return;

        if (ctx.getActiveLoots().isEmpty()) {
            Console.showMessage("There are no prizes on the ground to collect!");
            return;
        }

        Iterator<LootItem> it = ctx.getActiveLoots().iterator();
        while (it.hasNext()) {
            LootItem loot = it.next();
            switch (loot.getType()) {
                case COIN -> {
                    user.setCoins(user.getCoins() + 50);
                    Console.showMessage("Collected 50 coins! Total coins: " + user.getCoins());
                }
                case DIAMOND -> {
                    user.setGems(user.getGems() + 1);
                    Console.showMessage("Collected 1 diamond! Total diamonds: " + user.getGems());
                }
                case POT -> {
                    user.setOwnedPotsCount(user.getOwnedPotsCount() + 1);
                    Console.showMessage("Collected 1 pot! Total pots: " + user.getOwnedPotsCount());
                }
            }
            it.remove();
        }
    }
}
