package model.mechanisms;

import model.GameContext;
import model.user.User;
import model.user.UserManager;
import view.ConsoleView;

import java.util.Random;

public class LootItem {
    public enum LootType {
        COIN, DIAMOND, POT
    }

    private static final Random random = new Random();

    private final LootType type;
    private final int x;
    private final int y;

    public LootItem(LootType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public LootType getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }

    public static void tryDropLoot(GameContext ctx, int x, int y) {
        if (random.nextInt(100) > 10) {
            return;
        }

        int lootRoll = random.nextInt(3);
        User currentUser = UserManager.getInstance().getCurrentUser();

        if (lootRoll == 0) {
            ctx.addLoot(new LootItem(LootType.COIN, x, y));
            if (currentUser != null) {
                ConsoleView.showMessage("A zombie dropeed a coin; you have should collect it.");
            }
        } else if (lootRoll == 1) {
            ctx.addLoot(new LootItem(LootType.DIAMOND, x, y));
            if (currentUser != null) {
                ConsoleView.showMessage("A zombie dropeed a diamond; yyou have should collect it.");
            }
        } else {
            ctx.addLoot(new LootItem(LootType.POT, x, y));
            if (currentUser != null) {
                ConsoleView.showMessage("A zombie dropeed a pot; you have should collect it.");
            }
        }
    }
}