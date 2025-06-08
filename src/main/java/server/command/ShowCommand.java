package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

import java.util.stream.Collectors;

/**
 * Команда show: выводит все элементы коллекции
 */
public class ShowCommand implements Command{
    private final CollectionManager cm;

    public ShowCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request){

        String body = cm.getCollection().stream().map(Organization::toString).collect(Collectors.joining("\n---\n"));
        if (body.isEmpty()){
            body = "Коллекция пуста";
        }
        return new Response(body);
    }

    @Override
    public String getName() {
        return "show";
    }
}
