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
}
