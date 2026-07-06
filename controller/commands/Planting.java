package controller.commands;

import controller.commandHandler.Command;

public class Planting implements Command {
    @Override
    public void execute(String[] args) {//اگه لول Conveyor belt بود نیازی به افتاب نداریم. از نوار انتخاب کن

    }

    //Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.
    /*
    public void handlePlantCommand(String plantName, int row, int col, GameContext ctx) throws Exception {
    if (ctx.getLevel().getSpecialLevelType() == Level.SpecialLevelType.CONVEYOR_BELT) {

        // ۱. پیدا کردن گیاه روی نوار نقاله
        Plant plantToPlant = null;
        for (Plant p : ctx.getConveyorBelt()) {
            if (p.getName().equalsIgnoreCase(plantName)) {
                plantToPlant = p;
                break;
            }
        }

        if (plantToPlant == null) {
            throw new Exception("This plant is not available on the conveyor belt right now!");
        }

        // ۲. کاشت گیاه روی زمین بدون کم کردن خورشید
        ctx.getPlantGrid()[row][col] = plantToPlant;
        ctx.getActivePlants().add(plantToPlant);

        // ۳. حذف کارت از روی نوار نقاله
        ctx.getConveyorBelt().remove(plantToPlant);

    } else {
        // --- منطق مراحل معمولی بازی ---
        // ۱. چک کردن موجودی خورشید (sunAmount)
        // ۲. چک کردن کول‌داون کارت‌ها و در نهایت کاشت گیاه
    }
}
     */
}
