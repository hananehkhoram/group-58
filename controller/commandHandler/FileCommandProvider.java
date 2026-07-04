package controller.commandHandler;

import controller.MenuManager;
import controller.commands.*;
import controller.commands.GameMenuCommands.*;
import controller.commands.CollectionMenuCommands.*;
import controller.commands.GreenHouseCommands.CollectCommand;
import controller.commands.GreenHouseCommands.FasterGrow;
import controller.commands.GreenHouseCommands.PlantPot;
import controller.commands.GreenHouseCommands.ShowGreenHouse;
import controller.commands.NewsMenuCommands.ShowAllNews;
import controller.commands.NewsMenuCommands.ShowUnreadNews;
import controller.commands.PlantFoodCommands.FeedPlant;
import controller.commands.ProfileMenuCommands.*;
import controller.commands.ShopCommands.BuyCommand;
import controller.commands.GreenHouseCommands.EnterShop;
import controller.commands.ShopCommands.ShowProductsCommands;
import controller.commands.SpecialLevelsCommands.PlantWhatYouGet;
import controller.commands.status.show_map;
import controller.commands.status.show_plants_status;
import controller.commands.status.show_tile_status;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class FileCommandProvider implements controller.commandHandler.CommandProvider {

    private MenuManager menuManager;

    public FileCommandProvider(MenuManager menuManager){
        this.menuManager = menuManager;
    }


    @Override
    public void registerCommands (controller.commandHandler.CommandRegistry registry){
        // registry.register("regex", (args) -> model.executeCommand(args[0]));
        // other commands
        registry.register("menu enter (?<menuName>\\w*)" , new MenuEnter(this.menuManager));
        registry.register("menu show current" , new MenuShowCurrent(menuManager));
        registry.register("menu exit" , new MenuExit());
        registry.register("^register -u (?<username>\\S+) -p (?<password>\\S+) (?<passwordConfirm>\\S+) -n (?<nickname>\\S+) -e (?<email>\\S+) -g (?<gender>\\w+)$" , new NewUser());
        registry.register("pick question -q (?<questionNumber>\\d+) -a (?<answer>\\S+) -c (?<answerConfirm>\\S+)" , new PickAQuestion());
        registry.register("login -u (?<username>\\S+) -p (?<password>\\S+) -stayLoggedIn" , new controller.commands.LoginMenuCommands.Login(menuManager));
        registry.register("forget password -u (?<username>\\S+) -e (?<email>\\S+)" , new controller.commands.LoginMenuCommands.ForgetPassword(menuManager));
        registry.register("answer -a (?<answer>\\S+)" , new controller.commands.LoginMenuCommands.Answer(menuManager));
        registry.register("menu logout" , new controller.commands.MainMenuCommands.Logout());
        registry.register("menu enter chapter -c (?<chaptername>\\S+)" , new EnterChapter(menuManager));
        registry.register("menu greenhouse" , new EnterGreenHouse(menuManager));
        registry.register("menu travel-log" , new EnterTravelLog(menuManager));
        registry.register("menu leaderboard" , new EnterLeaderBoard(menuManager));
        registry.register("menu coin-wallet" , new EnterCoinWallet(menuManager));
        registry.register("menu gem-wallet" , new EnterGemWallet(menuManager));
        registry.register("" , new ChooseTheWorld(menuManager));
        registry.register("menu settings changeDifficulty -l (?<difficultyLevel>\\d+)" , new SettingsMenuCommands(menuManager));
        registry.register("menu news show-unread" , new ShowUnreadNews(menuManager));
        registry.register("menu news show-all" , new ShowAllNews(menuManager));
        registry.register("menu profile change-username -u (?<username>\\S+)" , new ChangeUsername(menuManager));
        registry.register("menu profile change-nickname -u (?<nickname>\\S+)" , new ChangeNickName(menuManager));
        registry.register("menu profile change-email -e (?<email>\\S+)" , new ChangeEmail(menuManager));
        registry.register("menu profile change-password -p (?<newPassword>\\S+) -o (?<oldPassword>\\S+)" , new ChangePassword(menuManager));
        registry.register("menu profile show-info" , new ShowInfo(menuManager));
        registry.register("menu collection show-(?<g>\\S+) plants" , new ShowPlantsCollection(menuManager));
        registry.register("menu collection show-(?<d>\\S+) zombies" , new ShowZombiesCollection(menuManager));
        registry.register("menu collection show-plant -p (?<plantName>\\S+)" , new ShowPlantDetails(menuManager));
        registry.register("menu collection show-zombie -z (?<zombieName>\\S+)" , new ShowZombieDetails(menuManager));
        registry.register("menu collection upgrade-plant -p (?<plantName>\\S+)" , new UpgradePlant(menuManager));
        registry.register("menu collection purchase-plant -p (?<plantName>\\S+)" , new PurchasePlant(menuManager));
        registry.register("show (?<allORavailable>) plants" , new controller.commands.PlantsList.ShowPlantsList(menuManager));
        registry.register("add plant -t (?<type>\\S+)" , new controller.commands.PlantsList.AddPlant(menuManager));
        registry.register("remove plant -t (?<type>\\S+)" , new controller.commands.PlantsList.RemovePlant(menuManager));
        registry.register("boost plant -t (?<type>\\S+)" , new controller.commands.PlantsList.BoostPlant(menuManager));
        registry.register("start game" , new StartGame(menuManager));
        registry.register("advance time -t (?<count>\\d+) ticks" , new controller.commands.MechanismsCommands.AdvancedTime());
        registry.register("collect sun -l ((?<x>\\d+), (?<y>\\d+))" , new controller.commands.MechanismsCommands.CollectSun());
        registry.register("show sun amount" , new controller.commands.MechanismsCommands.ShowSunAmount());
        registry.register("plant plant -t (?<type>\\S+) -l ((?<x>\\d+), (?<y>\\d+))" , new Planting());
        registry.register("pluck plant -l ((?<x>\\d+), (?<y>\\d+))" , new Plucking());
        registry.register("feed plant -l ((?<x>\\d+), (?<y>\\d+))" , new FeedPlant());
        registry.register("show map" , new show_map());
        registry.register("show plants status" , new show_plants_status());
        registry.register("show tile status -l ((?<x>\\d+), (?<y>\\d+))" , new show_tile_status());
        registry.register("zombies info" , new ShowZombiesInfo());
        registry.register("cheat spawn-zombie -t (?<zombieType>\\S+) -l <(?<x>\\d+), (?<y>\\d+)>" , new CheatSpawnZombie());
        registry.register("start zombie waves" , new PlantWhatYouGet());
        registry.register("show greenhouse" , new ShowGreenHouse(menuManager));
        registry.register("plant pot at ((?<x>\\d+), (?<y>\\d+))" , new PlantPot(menuManager));
        registry.register("collect ((?<x>\\d+), (?<y>\\d+))" , new CollectCommand(menuManager));
        registry.register("grow ((?<x>\\d+), (?<y>\\d+))" , new FasterGrow(menuManager));
        registry.register("enter shop" , new EnterShop(menuManager));
        registry.register("shop (?<listORdaily>\\S+)" , new ShowProductsCommands(menuManager));
        registry.register("shop buy -i (?<itemId>\\S+) -n (?<count>\\d+) [-t (?<plantType>\\S+)]" , new BuyCommand(menuManager));
        registry.register("travel log page (?<pageName>\\S+)" , new ShowQuests());

    }
}

