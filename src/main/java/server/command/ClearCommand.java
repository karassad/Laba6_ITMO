package server.command;

import dataBase.DatabaseManager;
import dataBase.UserManager;
import server.CollectionManager;
import shared.Request;
import shared.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Команда clear: очищает коллекцию организаций.
 * Проверяет владельца
 */
public class ClearCommand implements Command {
    private final CollectionManager cm;

    public ClearCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {

        int userId = UserManager.getUserId(request.getUsername());
        if (userId == -1) {
            return new Response("Ошибка авторизации.");
        }

        boolean cleared = cm.clearByUser(userId);
        return cleared
                ? new Response("Ваши элементы успешно удалены.")
                : new Response("У вас не было элементов для удаления.");

        //cm.clear();
        //return new Response("Коллекция очищена.");
    }

    @Override
    public String getName() {
        return "clear";
    }



}
