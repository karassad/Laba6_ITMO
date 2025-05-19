package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;

/**
 * Команда save: сохраняет коллекцию в файл (доступна только на сервере).
 */
public class SaveCommand implements Command {
    private final CollectionManager cm;

    public SaveCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        try {
            cm.save();
            return new Response("Коллекция успешно сохранена.");
        } catch (Exception e) {
            return new Response("Ошибка при сохранении коллекции: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "save";
    }
}
