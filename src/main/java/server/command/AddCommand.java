package server.command;

import dataBase.DatabaseManager;
import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;
import dataBase.UserManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Команда add: добавляет новую организацию в коллекцию + сохр айди клиента
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

        //полулчаем имя, потом через бд айди клиента
        int userId = UserManager.getUserId(request.getUsername());
        if (userId == -1) {
            return new Response("Ошибка: пользователь не найден.");
        }

        //привязываем организацию к пользователю
        org.setUserId(userId);

        //добавляем орг в бд
        boolean success = cm.addToDatabase(org);
        if (success) {
            cm.add(org); //в коллекцию
            return new Response("Организация добавлена: id=" + org.getId());
        } else {
            return new Response("Ошибка при сохранении в базу данных.");
        }
//        cm.add(org);
//        return new Response("Организация добавлена: id=" + org.getId());
    }

    @Override
    public String getName() {
        return "add";
    }




}
