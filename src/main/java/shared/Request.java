package shared;

import java.io.Serializable;

/**
 * Class representing a request from client to server.
 */
public class Request implements Serializable {

    private final String commandName;
    private final Object argument; // может быть id, Organization, String и т.п.

    private final String username;
    private final String password;

    public Request(String commandName, Object argument, String username, String password) {
        this.commandName = commandName;
        this.argument = argument;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCommandName() {
        return commandName;
    }

    public Object getArgument() {
        return argument;
    }

    public static Request parse(String line, String username, String password) {
        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";
        return new Request(cmd, args, username, password);
    }

}
