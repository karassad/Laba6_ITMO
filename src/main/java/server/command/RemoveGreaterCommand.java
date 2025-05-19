package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

/**
 * Команда remove_greater: удаляет из коллекции все элементы,
 * строго превышающие заданный.
 */
public class RemoveGreaterCommand implements Command {
    private final CollectionManager cm;

    public RemoveGreaterCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        Object arg = request.getArgument();
        if (!(arg instanceof Organization)) {
            return new Response("Ошибка: remove_greater требует аргумент Organization.");
        }
        Organization ref = (Organization) arg;
        int before = cm.getCollection().size();
        cm.getCollection().removeIf(o -> o.compareTo(ref) > 0);
        int removed = before - cm.getCollection().size();
        return new Response("Удалено " + removed + " элементов.");
    }

    @Override
    public String getName() {
        return "remove_greater";
    }
}
