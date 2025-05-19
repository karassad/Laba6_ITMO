package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

/**
 * Команда add: добавляет новую организацию в коллекцию.
 */
public class AddCommand implements Command{
    private final CollectionManager cm;


    public AddCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request){
        // Ожидаем, что аргументом будет объект Organization
        Object arg = request.getArgument();
        if (!(arg instanceof Organization)) {
            return new Response("Ошибка: аргумент команды add должен быть Organization.");
        }
        Organization org = (Organization) arg;
        cm.add(org);
        return new Response("Организация добавлена: id=" + org.getId());
    }

    @Override
    public String getName() {
        return "add";
    }



}
