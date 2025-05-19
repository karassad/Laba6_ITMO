package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

import java.util.Comparator;
import java.util.Optional;

/**
 * Команда min_by_creation_date: возвращает любой объект,
 * чьё поле creationDate минимально.
 */
public class MinByCreationDateCommand implements Command {
    private final CollectionManager cm;

    public MinByCreationDateCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        Optional<Organization> min = cm.getCollection().stream()
                .min(Comparator.comparing(Organization::getCreationDate));
        if (min.isPresent()) {
            return new Response(min.get().toString());
        } else {
            return new Response("Коллекция пуста.");
        }
    }

    @Override
    public String getName() {
        return "min_by_creation_date";
    }
}
