import controller.MenuManager;
import controller.repository.DataManager;
import model.menus.allmenus.RegisterMenu;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        MenuManager menuManager = new MenuManager();
        menuManager.changeMenu("RegisterMenu");
    }}
