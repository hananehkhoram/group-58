package model.MiniGame.Beghouled;

import controller.MenuManager;
import controller.repository.factory.LevelFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.season.Season;
import model.season.miniGameSeason.BeghouledSeason;

import java.util.List;

public class BeghouldGame {
    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;
    private BeghouledManager beghouledManager;

    public void start() {
        // ۱. ساخت لول‌ها و گرفتن لول اول
        List<Level> beghouledLevels = LevelFactory.buldBeghouledLevels();
        this.currentLevel = beghouledLevels.get(0);

        // ۲. تنظیم سیزن مربوط به بیجولد
        Season beghouledSeason = new BeghouledSeason(beghouledLevels);

        // ۳. ساخت کانتکست و انجین بازی
        this.ctx = new GameContext(this.currentLevel, beghouledSeason);
        this.gameEngine = new GameEngine(this.ctx, new MenuManager(ctx));
        this.ctx.setGameEngine(this.gameEngine);

        // ۴. راه‌اندازی منیجر بیجولد برای پازل (با هدف فرضاً ۱۰ مچ)
        this.beghouledManager = new BeghouledManager(this.ctx, this.gameEngine, 10);
        this.beghouledManager.initBoard();

        // ۵. فعال کردن شروع نبرد تا موج‌های زامبی طبق لول اسپاون شوند
        this.ctx.setBattleStarted(true);

        System.out.print("Beghouled Game Started\n");
    }

    // پیش بردن زمان و به‌روزرسانی موتور بازی برای حرکت زامبی‌ها
    public void advancedTimeCommand(double sec) {
        if (this.gameEngine != null && this.ctx != null) {
            int ticks = (int) (sec * 10);

            if (this.ctx.getTimeManager() != null) {
                this.ctx.getTimeManager().advanceTime(ticks);
            }
            this.gameEngine.update(sec);
        } else {
            view.ConsoleView.showMessage("Game engine is null");
        }
    }

    public GameContext getCtx(){
        return this.ctx;
    }

    public GameEngine getGameEngine(){
        return this.gameEngine;
    }

    public BeghouledManager getBeghouledManager() {
        return this.beghouledManager;
    }
}
