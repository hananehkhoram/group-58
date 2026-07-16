package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import controller.repository.DataManager;
import model.Quest;
import model.level.Level;
import model.season.Season;
import model.user.User;
import model.user.UserManager;
import view.ConsoleView;

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
        if (args == null || args.length == 0) {
            ConsoleView.showMessage("Please specify a page name.");
            return;
        }

        String pageName = args[0].toLowerCase();
        User user = UserManager.getInstance().getCurrentUser();
        if (user == null) {
            ConsoleView.showMessage("You must be logged in.");
            return;
        }

        switch (pageName) {
            case "daily" -> showQuestPage(user, Quest.QuestCategory.DAILY, "Daily Quests");
            case "main", "adventure" -> showQuestPage(user, Quest.QuestCategory.MAIN, "Main Quests");
            case "epic", "special", "challenge" -> showQuestPage(user, Quest.QuestCategory.EPIC, "Epic Challenges");
            case "minigames" -> showMinigamesPage(user);
            default -> ConsoleView.showMessage("Invalid page name. Try: daily, main, epic, minigames");
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
                    .append(q.getName()).append(" - ").append(q.getDescription());
            if (!done && q.getTargetProgress() > 1) {
                sb.append(" (").append(progress).append("/").append(q.getTargetProgress()).append(")");
            }
            sb.append("\n");
        }
        if (quests.isEmpty()) {
            sb.append("No quests in this page.\n");
        }
        ConsoleView.showMessage(sb.toString());
    }

    private void showMinigamesPage(User user) {
        StringBuilder sb = new StringBuilder("=== Minigames ===\n");
        for (Season s : DataManager.getInstance().seasons.getAll()) {
            if (MAIN_CHAPTER_NAMES.contains(s.getName())) continue;

            sb.append(s.getName()).append(":\n");
            for (Level lvl : s.getLevels()) {
                boolean unlocked = user.isLevelUnlocked(lvl.getName());
                sb.append("  ").append(unlocked ? "[UNLOCKED] " : "[LOCKED] ").append(lvl.getName()).append("\n");
            }
        }
        ConsoleView.showMessage(sb.toString());
    }
}
//travel log page <page_name>