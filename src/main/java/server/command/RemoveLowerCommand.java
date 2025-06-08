package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;
import dataBase.UserManager;

public class RemoveLowerCommand implements Command {
    private final CollectionManager cm;

    public RemoveLowerCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        Object arg = request.getArgument();
        if (!(arg instanceof Organization)) {
            return new Response("Ошибка: remove_lower требует аргумент Organization.");
        }

        Organization ref = (Organization) arg;
        int userId = UserManager.getUserId(request.getUsername());
        if (userId == -1) {
            return new Response("Ошибка авторизации.");
        }

        int removed = cm.removeLower(ref, userId);

        return new Response("Удалено " + removed + " ваших элементов, меньших заданного.");
    }

    @Override
    public String getName() {
        return "remove_lower";
    }
}
