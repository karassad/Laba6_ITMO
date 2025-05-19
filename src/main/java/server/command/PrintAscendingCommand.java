package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

import java.util.stream.Collectors;

/**
 * Команда print_ascending: выводит элементы коллекции в порядке возрастания.
 */
public class PrintAscendingCommand implements Command {
    private final CollectionManager cm;

    public PrintAscendingCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        String body = cm.getCollection().stream()
                .sorted()  // Comparable<Organization> по name
                .map(Organization::toString)
                .collect(Collectors.joining("\n---\n"));

        if (body.isBlank()) {
            body = "Коллекция пустая.";
        }
        return new Response(body);
    }

    @Override
    public String getName() {
        return "print_ascending";
    }
}
