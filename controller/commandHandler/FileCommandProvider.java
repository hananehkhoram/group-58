package controller.commandHandler;

import controller.MenuManager;
import controller.commands.*;
import controller.commands.GameMenuCommands.*;
import controller.commands.CollectionMenuCommands.*;
import controller.commands.GreenHouseCommands.CollectCommand;
import controller.commands.GreenHouseCommands.FasterGrow;
import controller.commands.GreenHouseCommands.PlantPot;
import controller.commands.GreenHouseCommands.ShowGreenHouse;
import controller.commands.LoginMenuCommands.SetNewPassword;
import controller.commands.PlantFoodCommands.CheatAddPlantFood;
import controller.commands.PlantFoodCommands.ShowCurrentPlantFood;
import controller.commands.PlantsList.StartGame;
import controller.commands.TravelMenuCommands.EnterMiniGameMenu;
import controller.commands.MechanismsCommands.CheatAddSun;
import controller.commands.NewsMenuCommands.ShowAllNews;
import controller.commands.NewsMenuCommands.ShowUnreadNews;
import controller.commands.PlantFoodCommands.FeedPlant;
import controller.commands.ProfileMenuCommands.*;
import controller.commands.RegisterMenuCommands.NewUser;
import controller.commands.RegisterMenuCommands.PickAQuestion;
import controller.commands.ShopCommands.BuyCommand;
import controller.commands.GreenHouseCommands.EnterShop;
import controller.commands.ShopCommands.ShowProductsCommands;
import controller.commands.SpecialLevelsCommands.PlantWhatYouGet;
import controller.commands.TravelMenuCommands.ShowTravelMenu;
import controller.commands.Status.ShowMap;
import controller.commands.Status.ShowPlantsStatus;
import controller.commands.Status.ShowTileStatus;
import controller.commands.TravelMenuCommands.SmashVase;
import model.GameContext;


public class FileCommandProvider implements controller.commandHandler.CommandProvider {

    private MenuManager menuManager;
    private GameContext ctx;

    public FileCommandProvider(MenuManager menuManager, GameContext ctx) {
        this.menuManager = menuManager;
        this.ctx = ctx;
    }


    @Override
    public void registerCommands (controller.commandHandler.CommandRegistry registry){
        // registry.register("regex", (args) -> model.executeCommand(args[0]));
        // other commands
        registry.register("menu enter (?<menuName>\\w*)" , new MenuEnter(this.menuManager));
        registry.register("menu show current" , new MenuShowCurrent(menuManager));
        registry.register("menu exit" , new MenuExit(menuManager));
        registry.register("^register -u (?<username>\\S+) -p (?<password>\\S+) (?<passwordConfirm>\\S+) -n (?<nickname>\\S+) -e (?<email>\\S+) -g (?<gender>\\w+)$" , new NewUser(menuManager));
        registry.register("pick question -q (?<questionNumber>\\d+) -a (?<answer>.+) -c (?<answerConfirm>.+)" , new PickAQuestion(menuManager));
        registry.register("login -u (?<username>\\S+) -p (?<password>\\S+)(?<stayLoggedIn> -stay-logged-in)?" , new controller.commands.LoginMenuCommands.Login(menuManager));
        registry.register("forget password -u (?<username>\\S+) -e (?<email>\\S+)" , new controller.commands.LoginMenuCommands.ForgetPassword(menuManager));
        registry.register("answer -a (?<answer>.+)" , new controller.commands.LoginMenuCommands.Answer(menuManager));
        registry.register("menu logout" , new controller.commands.MainMenuCommands.Logout(menuManager));
        registry.register("menu enter chapter -c (?<chaptername>.+?)(?: -l (?<levelNumber>\\d+))?" , new EnterChapter(menuManager));
        registry.register("menu greenhouse" , new EnterGreenHouse(menuManager));
        registry.register("menu travel-log" , new EnterTravelLog(menuManager));
        registry.register("menu leaderboard" , new EnterLeaderBoard(menuManager, ctx));
        registry.register("menu coin-wallet" , new EnterCoinWallet(menuManager));
        registry.register("menu gem-wallet" , new EnterGemWallet(menuManager));
        registry.register("choose world -w (?<worldName>.+)" , new ChooseTheWorld(menuManager));
        registry.register("menu settings change-difficulty -l (?<difficultyLevel>\\d+)" , new SettingsMenuCommands(menuManager));
        registry.register("menu news show-unread" , new ShowUnreadNews(menuManager));
        registry.register("menu news show-all" , new ShowAllNews(menuManager));
        registry.register("menu profile change-username -u (?<username>\\S+)" , new ChangeUsername(menuManager));
        registry.register("menu profile change-nickname -u (?<nickname>\\S+)" , new ChangeNickName(menuManager));
        registry.register("menu profile change-email -e (?<email>\\S+)" , new ChangeEmail(menuManager));
        registry.register("menu profile change-password -p (?<newPassword>\\S+) -o (?<oldPassword>\\S+)" , new ChangePassword(menuManager));
        registry.register("menu profile show-info" , new ShowInfo(menuManager));
        registry.register("^menu collection show-(?:(?<g>all)-)?plants$" , new ShowPlantsCollection(menuManager));
        registry.register("^menu collection show-(?:(?<d>all)-)?zombies$" , new ShowZombiesCollection(menuManager));
        registry.register("menu collection show-plant -p (?<plantName>.+)" , new ShowPlantDetails(menuManager));
        registry.register("menu collection show-zombie -z (?<zombieName>.+)" , new ShowZombieDetails(menuManager));
        registry.register("menu collection upgrade-plant -p (?<plantName>.+)" , new UpgradePlant(menuManager));
        registry.register("^menu collection purchase-plant -p (?<plantName>.+)$" , new PurchasePlant(menuManager));
        registry.register("show (?<allORavailable>all|available) plants" , new controller.commands.PlantsList.ShowPlantsList(menuManager));
        registry.register("add plant -t (?<type>.+)" , new controller.commands.PlantsList.AddPlant(menuManager));
        registry.register("remove plant -t (?<type>.+)" , new controller.commands.PlantsList.RemovePlant(menuManager));
        registry.register("boost plant -t (?<type>.+)" , new controller.commands.PlantsList.BoostPlant(menuManager));
        registry.register("start game" , new StartGame(menuManager));
        registry.register("advance time -t (?<count>\\d+) ticks" , new controller.commands.MechanismsCommands.AdvancedTime(menuManager));
        registry.register("collect sun -l \\((?<x>\\d+), (?<y>\\d+)\\)" , new controller.commands.MechanismsCommands.CollectSun(menuManager));
        registry.register("show sun amount" , new controller.commands.MechanismsCommands.ShowSunAmount(menuManager));
        registry.register("^plant plant -t (?<type>.+?) -l \\((?<x>\\d+), (?<y>\\d+)\\)$" , new Planting(menuManager));
        registry.register("pluck plant -l \\((?<x>\\d+), (?<y>\\d+)\\)" , new Plucking(menuManager));
        registry.register("feed plant -l \\((?<x>\\d+), (?<y>\\d+)\\)" , new FeedPlant(menuManager));
        registry.register("show map" , new ShowMap(menuManager));
        registry.register("show plants status" , new ShowPlantsStatus(menuManager));
        registry.register("show tile status -l \\((?<x>\\d+), (?<y>\\d+)\\)" , new ShowTileStatus(menuManager));
        registry.register("zombies info" , new ShowZombiesInfo());
        registry.register("cheat spawn-zombie -t (?<zombieType>.+) -l \\((?<x>\\d+), (?<y>\\d+)\\)" , new CheatSpawnZombie(menuManager));
        registry.register("start zombie waves" , new PlantWhatYouGet());
        registry.register("show greenhouse" , new ShowGreenHouse(menuManager));
        registry.register("plant pot at \\((?<x>\\d+), (?<y>\\d+)\\)" , new PlantPot(menuManager));
        registry.register("collect \\((?<x>\\d+), (?<y>\\d+)\\)" , new CollectCommand(menuManager));
        registry.register("grow \\((?<x>\\d+), (?<y>\\d+)\\)" , new FasterGrow(menuManager));
        registry.register("enter shop" , new EnterShop(menuManager));
        registry.register("shop (?<listORdaily>\\S+)" , new ShowProductsCommands(menuManager));
        registry.register("^shop buy -i (?<itemId>\\d+) -n (?<count>\\d+)(?:\\s+-t\\s+(?<plantType>.+))?$" , new BuyCommand(menuManager));
        registry.register("travel log page (?<pageName>.+)" , new ShowTravelMenu(menuManager));
        registry.register("(?i)^menu\\s+cheat\\s+add\\s+(\\d+)\\s+(coin|diamond)$",new CheatAddCurrency(menuManager));
        registry.register("cheat reset users",new CheatClearUsers());
        registry.register("release the nuke", new ReleaseTheNuke(menuManager));
        registry.register("cheat add -n (?<count>\\d+) suns",new CheatAddSun(menuManager));
        registry.register("(?<enter>enter) minigame" , new EnterMiniGameMenu(menuManager));
        registry.register("(?<number>\\d+)" , new EnterMiniGameMenu(menuManager));
        registry.register("cheat remove-cooldown",new CheatRemoveCooldown(menuManager));
        registry.register("^new password -p (?<password>.+)$",new SetNewPassword(menuManager));
        registry.register("smash vase -l ((?<x>\\d+), (?<y>\\d+))", new SmashVase(menuManager));
        registry.register("cheat add-plant-food",new CheatAddPlantFood(menuManager));
        registry.register("show current plant food",new ShowCurrentPlantFood(menuManager));;
    }
}

