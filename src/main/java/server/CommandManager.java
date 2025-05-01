package server;

import server.command.Command;
import shared.Request;
import shared.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for executing commands.
 */
public class CommandManager {

    private final Map<String, Command> commands = new HashMap<>(); //ключ - значение

    public CommandManager(Command... commandList) { // в конструктор можно передать любое количество объектов класса Command
        // и все они будут доступны как массив commandList
        for (Command command : commandList){
            commands.put(command.getName(), command);
        }
    }

    public Response execute(Request request){
        Command command = commands.get(request.getCommandName());
        if (command != null){
            return command.execute(request);
        } else {
            return new Response("Неизвестная команда. Используйте help для списка команд.");
        }

    }
}
