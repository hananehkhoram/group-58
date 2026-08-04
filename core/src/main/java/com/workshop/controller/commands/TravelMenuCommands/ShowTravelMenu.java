package com.workshop.controller.commands.TravelMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.Quest;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.TravelMenu;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ShowTravelMenu implements Command {
    private final MenuManager menuManager;

    private static final Set<String> MAIN_CHAPTER_NAMES = Set.of(
            "Ancient Egypt", "Frozen Caves", "Big Wave Beach", "Dark Ages"
    );

    public ShowTravelMenu(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof TravelMenu) {
            if (args == null || args.length == 0) {
                Console.showMessage("Please specify a page name.");
                return;
            }

            String pageName = args[0].toLowerCase();
            User user = UserManager.getInstance().getCurrentUser();
            if (user == null) {
                Console.showMessage("You must be logged in.");
                return;
            }

            switch (pageName) {
                case "daily" -> showQuestPage(user, Quest.QuestCategory.DAILY, "Daily Quests");
                case "main", "adventure" -> showQuestPage(user, Quest.QuestCategory.MAIN, "Main Quests");
                case "epic", "special", "challenge" -> showQuestPage(user, Quest.QuestCategory.EPIC, "Epic Challenges");
                case "minigames" -> Console.showMessage("Use enter minigame command.");
                default -> Console.showMessage("Invalid page name. Try: daily, main, epic, minigames");
            }
        }
    }

    private void showQuestPage(User user, Quest.QuestCategory category, String title) {
        List<Quest> quests = DataManager.getInstance().quests.getByCategory(category);
        quests = quests.stream()
                .sorted(Comparator.comparing(Quest::getPriority).reversed())
                .toList();

        StringBuilder sb = new StringBuilder("=== " + title + " ===\n");
        for (Quest q : quests) {
            boolean done = user.isQuestCompleted(q.getId());
            int progress = user.getQuestProgress(q.getId());
            sb.append(done ? "[DONE] " : "[ ] ")
                    .append("[").append(q.getPriority()).append("] ")
                    .append(q.getName()).append(" - ").append(q.getDescription());
            if (!done) {
                sb.append(q.getTargetProgress() > 1
                        ? " (" + progress + "/" + q.getTargetProgress() + ")"
                        : " (not completed yet)");
            }
            sb.append("\n");
        }
        if (quests.isEmpty()) {
            sb.append("No quests in this page.\n");
        }
        Console.showMessage(sb.toString());
    }
}
//travel log page <page_name>
