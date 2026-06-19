package controller.commandHandler;

import exceptions.CommandNotFound;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandRegistry {
    private static class CommandEntry{
        Pattern pattern;
        controller.commandHandler.Command command;

        CommandEntry(String regex, controller.commandHandler.Command command){
            this.pattern = Pattern.compile(regex);
            this.command = command;
        }
    }

    private final List<CommandEntry> commands = new ArrayList<>();
    public void register(String regex, controller.commandHandler.Command command){
        commands.add(new CommandEntry(regex, command));
    }
    public void handleCommand (String input){
        for (CommandEntry entry : commands){
            Matcher matcher = entry.pattern.matcher(input);
            if (matcher.matches()){
                String[] args = new String[matcher.groupCount()];
                for (int i = 0; i < matcher.groupCount(); i++){
                args[i] = matcher.group(i + 1);
                }
                entry.command.execute(args);
                return;
            }
        }
        throw new CommandNotFound("Invalid Command.");
    }
}
