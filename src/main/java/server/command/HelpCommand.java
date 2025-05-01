package server.command;

import shared.Request;
import shared.Response;

/**
 * Command to show help information.
 */
public class HelpCommand implements Command{

    @Override
    public Response execute(Request request){
        return new Response(String.join("\n",
                "Список доступных команд:",
                "help : вывести справку по доступным командам",
                "info : вывести информацию о коллекции",
                "show : вывести все элементы коллекции",
                "add {element} : добавить новый элемент в коллекцию",
                "clear : очистить коллекцию"

        ));
    }

    @Override
    public String getName() {
        return "help";
    }
}
