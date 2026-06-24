package view;

import java.util.Scanner;

public class ConsoleView {
    private static Scanner scanner;

    public ConsoleView() {
    }

    public static String getInput(String var0) {
        if (scanner.hasNextLine()) {
            System.out.print(var0);
            return scanner.nextLine().trim();
        } else {
            return "exit";
        }
    }

    public static void showMessage(String var0, Object... var1) {
        System.out.printf(var0 + "%n", var1);
    }

    public static void simplePrint(String var0, Object... var1) {
        System.out.printf(var0, var1);
    }

    public static boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    public static String nextLine() {
        return scanner.nextLine().trim();
    }

    static {
        scanner = new Scanner(System.in);
    }
}

