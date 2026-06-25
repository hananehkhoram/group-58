package controller.commandHandler;

import controller.MenuManager;
import controller.commands.*;
import controller.commands.GameMenuCommands.*;
import controller.commands.collectionMenuCommands.*;
import controller.commands.GreenHouseCommands.CollectCommand;
import controller.commands.GreenHouseCommands.FasterGrow;
import controller.commands.GreenHouseCommands.PlantPot;
import controller.commands.GreenHouseCommands.ShowGreenHouse;
import controller.commands.plant_food_commands.FeedPlant;
import controller.commands.profileMenuCommands.*;
import controller.commands.ShopCommands.BuyCommand;
import controller.commands.ShopCommands.EnterShop;
import controller.commands.ShopCommands.ShowProductsCommands;
import controller.commands.SpecialLevelsCommands.PlantWhatYouGet;
import controller.commands.status.show_map;
import controller.commands.status.show_plants_status;
import controller.commands.status.show_tile_status;


public class FileCommandProvider implements controller.commandHandler.CommandProvider {

    private MenuManager menuManager;

    public FileCommandProvider(MenuManager menuManager){
        this.menuManager = menuManager;
    }

    @Override
    public void registerCommands (controller.commandHandler.CommandRegistry registry){
        // registry.register("regex", (args) -> model.executeCommand(args[0]));
        // other commands
        registry.register("menu enter (?<menu_name>\\w*)" , new MenuEnter(this.menuManager));
        registry.register("menu show current" , new MenuShowCurrent());
        registry.register("menu exit" , new MenuExit());
        registry.register("^register -u (?<username>[A-Za-z0-9-]+) -p (?<password>\\S+) (?<password_confirm>\\S+) -n (?<nickname>\\S+) -e (?<email>\\S+) -g (?<gender>\\w+)$" , new NewUser());
        registry.register("pick question -q (?<question_number>\\d+) -a (?<answer>\\S+) -c (?<answer_confirm>\\S+)" , new PickAQuestion());
        registry.register("login -u (?<username>[A-Za-z0-9-]) -p (?<password>\\S+) -stay-logged-in" , new controller.commands.loginMenuCommands.Login());
        registry.register("forget password -u (?<username>[A-Za-z0-9-] -e (?<email>\\S+)" , new controller.commands.loginMenuCommands.ForgetPassword());
        registry.register("answer -a (?<answer>\\S+)" , new controller.commands.loginMenuCommands.Answer());
        registry.register("menu logout" , new controller.commands.mainMenuCommands.Logout());
        registry.register("menu enter chapter -c (?<chaptername>\\S+)" , new EnterChapter());
        registry.register("menu greenhouse" , new enterGreenHouse());
        registry.register("menu travel-log" , new EnterTravelLog());
        registry.register("menu leaderboard" , new EnterLeaderBoard());
        registry.register("menu coin-wallet" , new EnterCoinWallet());
        registry.register("menu gem-wallet" , new EnterGemWallet());
        registry.register("" , new ChooseTheWorld());
        registry.register("menu settings change-difficulty -l (?<difficulty_level>\\d+)" , new SettingsMenuCommands());
        registry.register("menu news show-unread" , new show_unreadNews());
        registry.register("menu news show-all" , new show_allNews());
        registry.register("menu profile change-username -u (?<username>\\S+)" , new change_username());
        registry.register("menu profile change-nickname -u (?<nickname>\\S+)" , new change_nickName());
        registry.register("menu profile change-email -e (?<email>\\S+)" , new change_email());
        registry.register("menu profile change-password -p (?<new_password>\\S+) -o (?<old_password>\\S+)" , new change_password());
        registry.register("menu profile show-info" , new showInfo());
        registry.register("menu collection show-(?<all/nothing>\\S+)plants" , new showPlantsCollection());
        registry.register("menu collection show-(?<all/nothing>)zombies" , new showZombiesCollection());
        registry.register("menu collection show-plant -p (?<plant_name>\\S+)" , new show_plant_details(menuManager));
        registry.register("menu collection show-zombie -z (?<zombie_name>\\S+)" , new show_zombie_details());
        registry.register("menu collection upgrade-plant -p (?<plant_name>\\S+)" , new upgradePlant());
        registry.register("menu collection purchase-plant -p (?<plant_name>\\S+)" , new purchasePlant(menuManager));
        registry.register("show (?<all/available>) plants" , new controller.commands.plantsList.ShowPlantsList());
        registry.register("add plant -t (?<type>\\S+)" , new controller.commands.plantsList.AddPlant());
        registry.register("remove plant -t (?<type>\\S+)" , new controller.commands.plantsList.RemovePlant());
        registry.register("boost plant -t (?<type>\\S+)" , new controller.commands.plantsList.BoostPlant());
        registry.register("start game" , new StartGame());
        registry.register("advance time -t (?<count>\\d+) ticks" , new controller.commands.mechanismsCommands.AdvancedTime());
        registry.register("collect sun -l ((?<x>\\d+), (?<y>\\d+))" , new controller.commands.mechanismsCommands.CollectSun());
        registry.register("show sun amount" , new controller.commands.mechanismsCommands.ShowSunAmount());
        registry.register("plant plant -t (?<type>\\S+) -l ((?<x>\\d+), (?<y>\\d+))" , new Planting());
        registry.register("pluck plant -l ((?<x>\\d+), (?<y>\\d+))" , new Plucking());
        registry.register("feed plant -l ((?<x>\\d+), (?<y>\\d+))" , new FeedPlant());
        registry.register("show map" , new show_map());
        registry.register("show plants status" , new show_plants_status());
        registry.register("show tile status -l ((?<x>\\d+), (?<y>\\d+))" , new show_tile_status());
        registry.register("zombies info" , new ShowZombiesInfo());
        registry.register("cheat spawn-zombie -t (?<zombie-type>\\S+) -l <(?<x>\\d+), (?<y>\\d+)>" , new CheatSpawnZombie());
        registry.register("start zombie waves" , new PlantWhatYouGet());
        registry.register("show greenhouse" , new ShowGreenHouse());
        registry.register("plant pot at ((?<x>\\d+), (?<y>\\d+))" , new PlantPot());
        registry.register("collect ((?<x>\\d+), (?<y>\\d+))" , new CollectCommand());
        registry.register("grow ((?<x>\\d+), (?<y>\\d+))" , new FasterGrow());
        registry.register("enter shop" , new EnterShop());
        registry.register("shop (?<list/daily>\\S+)" , new ShowProductsCommands());
        registry.register("shop buy -i (?<item_id>\\S+) -n (?<count>\\d+) [-t (?<plant_type>\\S+)]" , new BuyCommand());
        registry.register("travel log page (?<page_name>\\S+)" , new ShowQuests());

    }
}

