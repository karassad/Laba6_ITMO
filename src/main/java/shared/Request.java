package shared;

import java.io.Serializable;

/**
 * Class representing a request from client to server.
 */
public class Request implements Serializable {

    private final String commandName;
    private final Object argument; // может быть id, Organization, String и т.п.

    public Request(String commandName, Object argument) {
        this.commandName = commandName;
        this.argument = argument;
    }

    public String getCommandName() {
        return commandName;
    }

    public Object getArgument() {
        return argument;
    }

    public static Request parse(String line) {
        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";
        // для примитивов и элементарных – можно упаковать как строку, Int и т.д.
        // Здесь мы просто храним cmd и args, а разбор аргументов произойдёт в ConsoleManager на клиенте или в CommandManager на сервере:
        return new Request(cmd, args);
    }

}
