package controller.commands.GameMenuCommands;

import controller.GameEngineController;
import controller.MenuManager;
import controller.commandHandler.Command;
import controller.repository.DataManager;
import model.GameContext;
import model.level.Level;
import model.level.LevelType;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;
import model.season.Grave;
import model.season.Season;
import model.user.User;
import model.user.UserManager;
import view.ConsoleView;

import java.util.List;

public class EnterChapter implements Command {

    private MenuManager menuManager;

    public EnterChapter(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            ConsoleView.showMessage("Please enter a chapter name.");
            return;
        }

        // نام چپتر مستقیماً آرگومان اول (گروه اول رجکس) است
        String chapterName = args[0].trim();

        Season chapter = DataManager.getInstance().seasons.get(chapterName);

        if (chapter == null) {
            ConsoleView.showMessage("Chapter not found: " + chapterName);
            return;
        }
        if (!isChapterUnlocked(chapter, UserManager.getInstance().getCurrentUser())) {
            ConsoleView.showMessage("This chapter is locked.");
            return;
        }

        Menu currentMenu = menuManager.getCurrentMenu();
        Level levelToPlay;

        // اگر آرگومان دوم (شماره مرحله) وجود داشت و نال نبود
        if (args.length > 1 && args[1] != null) {
            int levelNumber = Integer.parseInt(args[1].trim());
            List<Level> levels = chapter.getLevels();
            if (levelNumber < 1 || levelNumber > levels.size()) {
                ConsoleView.showMessage("Invalid level number.");
                return;
            }
            Level requested = levels.get(levelNumber - 1);
            if (!UserManager.getInstance().getCurrentUser().isLevelUnlocked(requested.getName())) {
                ConsoleView.showMessage("This level is locked.");
                return;
            }
            levelToPlay = requested;
        } else {
            levelToPlay = firstUnfinishedLevel(chapter, UserManager.getInstance().getCurrentUser());
        }


        if (currentMenu instanceof GameMenu) {
            menuManager.startBattle(levelToPlay, chapter);
            if (levelToPlay.getLevelType().equals(LevelType.CONVEYOR_BELT)){
                menuManager.getCtx().getSeason().onLevelStart(menuManager.getCtx());

                for (Grave g : menuManager.getCtx().getSeason().getInitialGraves(menuManager.getCtx().getLevel())) {
                    menuManager.getCtx().placeGrave(g, g.getRow(), g.getCol());
                }

                ConsoleView.showMessage("Let's begin this level: %s\n" , menuManager.getCtx().getLevel().getName());
                menuManager.forceChangeMenu("gamemenu");
                menuManager.getCtx().setBattleStarted(true);
            }else {
                menuManager.changeMenu("plantselectionmenu");
                ConsoleView.showMessage("Entering %s. Choose your plants.", levelToPlay.getName());
            }
        }
    }

    private boolean isChapterUnlocked(Season chapter, User user) {
        if (chapter == null || chapter.getLevels().isEmpty()) {
            return false;
        }
        Level firstLevel = chapter.getLevels().get(0);
        if (DataManager.getInstance().seasons.get("Ancient Egypt") == chapter) {
            return true;
        }
        return user.isLevelUnlocked(firstLevel.getName());
    }

    private Level firstUnfinishedLevel(Season chapter, User user) {
        Level currentLevel = chapter.getLevels().get(0);
        for (Level lvl : chapter.getLevels()) {
            if (user.isLevelUnlocked(lvl.getName())) {
                currentLevel = lvl;
            } else {
                break;
            }
        }
        return currentLevel;
    }//menu enter chapter -c
}