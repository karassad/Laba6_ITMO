package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

import java.util.stream.Collectors;

/**
 * Команда filter_less_than_official_address officialAddress:
 * выводит все организации, у которых officialAddress.zipCode
 * лексикографически меньше заданного.
 */
public class FilterLessThanOfficialAddressCommand implements Command {
    private final CollectionManager cm;

    public FilterLessThanOfficialAddressCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        Object arg = request.getArgument();
        if (!(arg instanceof String)) {
            return new Response("Ошибка: filter_less_than_official_address требует строковый аргумент.");
        }
        String zip = (String) arg;
        String body = cm.getCollection().stream()
                .filter(o -> o.getOfficialAddress().getZipCode().compareTo(zip) < 0)
                .map(Organization::toString)
                .collect(Collectors.joining("\n---\n"));
        if (body.isBlank()) {
            body = "Ничего не найдено.";
        }
        return new Response(body);
    }

    @Override
    public String getName() {
        return "filter_less_than_official_address";
    }
}
