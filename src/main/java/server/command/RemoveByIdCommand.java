package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;

import dataBase.UserManager;

/**
 * Команда remove_by_id: удаляет элемент коллекции по его id.
 */
public class RemoveByIdCommand implements Command {
    private final CollectionManager cm;

    public RemoveByIdCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        Object arg = request.getArgument();
        if (!(arg instanceof Integer)) {
            return new Response("Ошибка: аргумент remove_by_id должен быть целым числом (id).");
        }
        int id = (Integer) arg;

        //получаем id клиента
        int userId = UserManager.getUserId(request.getUsername());
        if (userId == -1) {
            return new Response("Ошибка авторизации");
        }

        //пытаемся удалить объект из бд и локалки
        boolean removed = cm.removeByIdFromDatabase(id, userId);
        if (removed) {
            return new Response("Элемент с id=" + id + " удалён.");
        } else {
            return new Response("Элемент не найден или не принадлежит вам.");
        }
    }

    @Override
    public String getName() {
        return "remove_by_id";
    }
}
