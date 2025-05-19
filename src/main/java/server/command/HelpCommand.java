// server/command/HelpCommand.java
package server.command;

import shared.Request;
import shared.Response;

/**
 * Command to show help information.
 * Выводит полный список доступных команд.
 */
public class HelpCommand implements Command {

    @Override
    public Response execute(Request request) {
        return new Response(String.join("\n",
                "Список доступных команд:",
                "help : вывести справку по доступным командам",
                "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество и т.д.)",
                "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении",
                "add {element} : добавить новый элемент в коллекцию",
                "update id {element} : обновить значение элемента коллекции по указанному id",
                "remove_by_id id : удалить элемент из коллекции по его id",
                "clear : очистить коллекцию",
                "save : сохранить коллекцию в файл",
                "execute_script file_name : считать и исполнить скрипт из указанного файла",
                "exit : завершить программу (без сохранения в файл)",
                "add_if_max {element} : добавить новый элемент, если он больше максимального",
                "remove_lower {element} : удалить из коллекции все элементы, меньшие заданного",
                "history : вывести последние 8 команд (без аргументов)",
                "min_by_creation_date : вывести любой объект с минимальным creationDate",
                "filter_less_than_official_address officialAddress : вывести элементы, officialAddress которых меньше заданного",
                "filter_greater_than_type type : вывести элементы, type которых больше заданного",
                "print_ascending : вывести элементы коллекции в порядке возрастания"
        ));
    }

    @Override
    public String getName() {
        return "help";
    }
}
