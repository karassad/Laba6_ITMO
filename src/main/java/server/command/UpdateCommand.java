package server.command;

import dataBase.UserManager;
import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

/**
 * Команда update: обновляет организацию по id новыми данными.
 * Формат: update id {элемент}
 * редактировать могут только собственники
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

        //получаем айди по имени
        int userId = UserManager.getUserId(request.getUsername());
        if (userId == -1) {
            return new Response("Ошибка: пользователь не найден.");
        }

//        boolean ok = cm.updateById(id, newOrgTemplate);
        boolean ok = cm.updateInDatabase(id, newOrgTemplate, userId);
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
