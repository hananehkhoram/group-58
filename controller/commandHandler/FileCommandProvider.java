package controller.commandHandler;

import controller.MenuManager;
import controller.commands.*;
import controller.commands.GameMenuCommands.*;
import controller.commands.collectionMenuCommands.*;
import controller.commands.greenHouse_commands.collect_command;
import controller.commands.greenHouse_commands.faster_grow;
import controller.commands.greenHouse_commands.plant_pot;
import controller.commands.greenHouse_commands.show_greenHouse;
import controller.commands.loginMenuCommands.answer;
import controller.commands.loginMenuCommands.forget_password;
import controller.commands.loginMenuCommands.login;
import controller.commands.mainMenuCommands.logout;
import controller.commands.mechanismsCommands.advancedTime;
import controller.commands.mechanismsCommands.collect_sun;
import controller.commands.mechanismsCommands.show_sun_amount;
import controller.commands.plant_food_commands.feed_plant;
import controller.commands.plantsList.addPlant;
import controller.commands.plantsList.boostPlant;
import controller.commands.plantsList.removePlant;
import controller.commands.plantsList.show_plantsList;
import controller.commands.profileMenuCommands.*;
import controller.commands.shop_commands.buy_command;
import controller.commands.shop_commands.enter_shop;
import controller.commands.shop_commands.show_products_commands;
import controller.commands.special_levels_commands.plant_what_you_get;
import controller.commands.status.show_map;
import controller.commands.status.show_plants_status;
import controller.commands.status.show_tile_status;
import controller.newsMenu_commands.show_allNews;
import controller.newsMenu_commands.show_unreadNews;

public class FileCommandProvider implements controller.commandHandler.CommandProvider {

    private MenuManager menuManager;

    public FileCommandProvider(MenuManager menuManager){
        this.menuManager = menuManager;
    }

    @Override
    public void registerCommands (controller.commandHandler.CommandRegistry registry){
        // registry.register("regex", (args) -> model.executeCommand(args[0]));
        // other commands
        registry.register("menu enter (?<menu_name>\\w*)" , new menu_enter(this.menuManager));
        registry.register("menu show current" , new menu_show_current());
        registry.register("menu exit" , new menu_exit());
        registry.register("^register -u (?<username>[A-Za-z0-9-]+) -p (?<password>\\S+) (?<password_confirm>\\S+) -n (?<nickname>\\S+) -e (?<email>\\S+) -g (?<gender>\\w+)$" , new new_user());
        registry.register("pick question -q (?<question_number>\\d+) -a (?<answer>\\S+) -c (?<answer_confirm>\\S+)" , new pick_a_question());
        registry.register("login -u (?<username>[A-Za-z0-9-]) -p (?<password>\\S+) -stay-logged-in" , new login());
        registry.register("forget password -u (?<username>[A-Za-z0-9-] -e (?<email>\\S+)" , new forget_password());
        registry.register("answer -a (?<answer>\\S+)" , new answer());
        registry.register("menu logout" , new logout());
        registry.register("menu enter chapter -c (?<chaptername>\\S+)" , new enter_Chapter());
        registry.register("menu greenhouse" , new enter_greenHouse());
        registry.register("menu travel-log" , new enter_travel_log());
        registry.register("menu leaderboard" , new enter_leaderBoard());
        registry.register("menu coin-wallet" , new enter_coinWallet());
        registry.register("menu gem-wallet" , new enter_gemWallet());
        registry.register("" , new chooseTheWorld());
        registry.register("menu settings change-difficulty -l (?<difficulty_level>\\d+)" , new settingsMenu_commands());
        registry.register("menu news show-unread" , new show_unreadNews());
        registry.register("menu news show-all" , new show_allNews());
        registry.register("menu profile change-username -u (?<username>\\S+)" , new change_username());
        registry.register("menu profile change-nickname -u (?<nickname>\\S+)" , new change_nickName());
        registry.register("menu profile change-email -e (?<email>\\S+)" , new change_email());
        registry.register("menu profile change-password -p (?<new_password>\\S+) -o (?<old_password>\\S+)" , new change_password());
        registry.register("menu profile show-info" , new showInfo());
        registry.register("menu collection show-(?<all/nothing>\\S+)plants" , new showPlantsCollection());
        registry.register("menu collection show-(?<all/nothing>)zombies" , new showZombiesCollection());
        registry.register("menu collection show-plant -p (?<plant_name>\\S+)" , new show_plant_details());
        registry.register("menu collection show-zombie -z (?<zombie_name>\\S+)" , new show_zombie_details());
        registry.register("menu collection upgrade-plant -p (?<plant_name>\\S+)" , new upgradePlant());
        registry.register("menu collection purchase-plant -p (?<plant_name>\\S+)" , new purchasePlant());
        registry.register("show (?<all/available>) plants" , new show_plantsList());
        registry.register("add plant -t (?<type>\\S+)" , new addPlant());
        registry.register("remove plant -t (?<type>\\S+)" , new removePlant());
        registry.register("boost plant -t (?<type>\\S+)" , new boostPlant());
        registry.register("start game" , new startGame());
        registry.register("advance time -t (?<count>\\d+) ticks" , new advancedTime());
        registry.register("collect sun -l ((?<x>\\d+), (?<y>\\d+))" , new collect_sun());
        registry.register("show sun amount" , new show_sun_amount());
        registry.register("plant plant -t (?<type>\\S+) -l ((?<x>\\d+), (?<y>\\d+))" , new planting());
        registry.register("pluck plant -l ((?<x>\\d+), (?<y>\\d+))" , new plucking());
        registry.register("feed plant -l ((?<x>\\d+), (?<y>\\d+))" , new feed_plant());
        registry.register("show map" , new show_map());
        registry.register("show plants status" , new show_plants_status());
        registry.register("show tile status -l ((?<x>\\d+), (?<y>\\d+))" , new show_tile_status());
        registry.register("zombies info" , new show_zombies_info());
        registry.register("cheat spawn-zombie -t (?<zombie-type>\\S+) -l <(?<x>\\d+), (?<y>\\d+)>" , new cheat_spawn_zombie());
        registry.register("start zombie waves" , new plant_what_you_get());
        registry.register("show greenhouse" , new show_greenHouse());
        registry.register("plant pot at ((?<x>\\d+), (?<y>\\d+))" , new plant_pot());
        registry.register("collect ((?<x>\\d+), (?<y>\\d+))" , new collect_command());
        registry.register("grow ((?<x>\\d+), (?<y>\\d+))" , new faster_grow());
        registry.register("enter shop" , new enter_shop());
        registry.register("shop (?<list/daily>\\S+)" , new show_products_commands());
        registry.register("shop buy -i (?<item_id>\\S+) -n (?<count>\\d+) [-t (?<plant_type>\\S+)]" , new buy_command());
        registry.register("travel log page (?<page_name>\\S+)" , new show_quests());

    }
}

