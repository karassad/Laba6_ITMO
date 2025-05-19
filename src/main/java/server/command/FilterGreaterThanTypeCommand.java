package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.OrganizationType;

import java.util.stream.Collectors;

/**
 * Команда filter_greater_than_type type:
 * выводит все организации, у которых поле type
 * лексикографически больше заданного.
 */
public class FilterGreaterThanTypeCommand implements Command {
    private final CollectionManager cm;

    public FilterGreaterThanTypeCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        Object arg = request.getArgument();
        if (!(arg instanceof OrganizationType)) {
            return new Response("Ошибка: filter_greater_than_type требует аргумент типа OrganizationType.");
        }
        OrganizationType t = (OrganizationType) arg;
        String body = cm.getCollection().stream()
                .filter(o -> o.getType() != null && o.getType().compareTo(t) > 0)
                .map(Object::toString)
                .collect(Collectors.joining("\n---\n"));
        if (body.isBlank()) {
            body = "Ничего не найдено.";
        }
        return new Response(body);
    }

    @Override
    public String getName() {
        return "filter_greater_than_type";
    }
}
