package server;

import server.command.Command;
import shared.Request;
import shared.Response;

import java.util.HashMap;
import java.util.Map;

import dataBase.UserManager;

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

//    public Response execute(Request request){
//        Command command = commands.get(request.getCommandName());
//        if (command != null){
//            return command.execute(request);
//        } else {
//            return new Response("Неизвестная команда. Используйте help для списка команд.");
//        }
//
//    }

    public Response execute(Request request){
        String commandName = request.getCommandName();

        //обработкa register и login
        if (commandName.equals("register")) {
            boolean success = UserManager.register(request.getUsername(), request.getPassword());
            return new Response(success
                    ? "Регистрация успешна! Вы молодцы, так держать!!!!"
                    : "Ошибка: пользователь с таким логином уже существует :(((");
        }

        if (commandName.equals("login")) {
            boolean success = UserManager.login(request.getUsername(), request.getPassword());
            return new Response(success
                    ? "Вход выполнен! Вы умница!!!"
                    : "Неверный логин или пароль :(((");
        }

        //остальные команды через мапу с комиандами
        Command command = commands.get(commandName);
        if (command != null){
            return command.execute(request);
        } else {
            return new Response("Неизвестная команда. Используйте help для списка команд.");
        }
    }


    public void register(Command cmd) {
        commands.put(cmd.getName(), cmd);
    }
}
