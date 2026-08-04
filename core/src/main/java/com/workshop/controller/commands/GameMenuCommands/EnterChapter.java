package com.workshop.controller.commands.GameMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.level.Level;
import com.workshop.model.level.LevelType;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GameMenu;
import com.workshop.model.season.Grave;
import com.workshop.model.season.Season;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

import java.util.List;

public class EnterChapter implements Command {

    private MenuManager menuManager;

    public EnterChapter(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        String chapterName = (args != null && args.length > 0 && args[0] != null) ? args[0].trim() : null;

        Season chapter;
        if (chapterName != null) {
            chapter = DataManager.getInstance().seasons.get(chapterName);
            if (chapter == null) {
                Console.showMessage("Chapter not found: " + chapterName);
                return;
            }
        } else {
            if (!(currentMenu instanceof GameMenu)) {
                Console.showMessage("Please enter a chapter name.");
                return;
            }
            chapter = ((GameMenu) currentMenu).getCurrentWorld();
            if (chapter == null) {
                Console.showMessage("No chapter specified. Use 'choose world -w <worldName>' first, or" +
                        " 'menu enter chapter -c <chaptername>'.");
                return;
            }
        }

        if (!isChapterUnlocked(chapter, UserManager.getInstance().getCurrentUser())) {
            Console.showMessage("This chapter is locked.");
            return;
        }

        Level levelToPlay;

        if (args.length > 1 && args[1] != null) {
            int levelNumber = Integer.parseInt(args[1].trim());
            List<Level> levels = chapter.getLevels();
            if (levelNumber < 1 || levelNumber > levels.size()) {
                Console.showMessage("Invalid level number.");
                return;
            }
            Level requested = levels.get(levelNumber - 1);
            if (!UserManager.getInstance().getCurrentUser().isLevelUnlocked(requested.getName())) {
                Console.showMessage("This level is locked.");
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

                Console.showMessage("Let's begin this level: %s\n" ,
                        menuManager.getCtx().getLevel().getName());
                menuManager.forceChangeMenu("gamemenu");
                menuManager.getCtx().setBattleStarted(true);
            }else {
                menuManager.changeMenu("plantselectionmenu");
                Console.showMessage("Entering %s. Choose your plants.", levelToPlay.getName());
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
    }//enter chapter -c <chapter> -l <levelNumber>
}
