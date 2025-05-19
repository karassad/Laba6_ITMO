package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;

/**
 * Команда remove_by_id: удаляет элемент коллекции по его id.
 */
public class RemoveByIdCommand implements Command {
    private final CollectionManager cm;

    public RemoveByIdCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        Object arg = request.getArgument();
        if (!(arg instanceof Integer)) {
            return new Response("Ошибка: аргумент remove_by_id должен быть целым числом (id).");
        }
        int id = (Integer) arg;
        boolean removed = cm.removeById(id);  // <-- обращаемся к CollectionManager
        if (removed) {
            return new Response("Элемент с id=" + id + " удалён.");
        } else {
            return new Response("Элемент с id=" + id + " не найден.");
        }
    }

    @Override
    public String getName() {
        return "remove_by_id";
    }
}
