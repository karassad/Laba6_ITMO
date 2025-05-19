package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;

/**
 * Команда clear: очищает коллекцию организаций.
 */
public class ClearCommand implements Command {
    private final CollectionManager cm;

    public ClearCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        cm.clear();
        return new Response("Коллекция очищена.");
    }

    @Override
    public String getName() {
        return "clear";
    }
}
