package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

/**
 * Команда update: обновляет организацию по id новыми данными.
 * Формат: update id {элемент}
 */
public class UpdateCommand implements Command {
    private final CollectionManager cm;

    public UpdateCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        Object[] arr = (Object[]) request.getArgument();
        int id = (Integer) arr[0];
        Organization newOrgTemplate = (Organization) arr[1];

        boolean ok = cm.updateById(id, newOrgTemplate);
        if (ok) {
            return new Response("Организация с id=" + id + " обновлена.");
        } else {
            return new Response("Элемент с id=" + id + " не найден.");
        }
    }


    @Override
    public String getName() {
        return "update";
    }
}
