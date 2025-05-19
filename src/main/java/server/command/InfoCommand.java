package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;

/**
 * Команда info: выводит информацию о коллекции
 * (тип, дата инициализации, количество элементов).
 */
public class InfoCommand implements Command{
    private final CollectionManager cm;

    public InfoCommand(CollectionManager collectionManager) {
        this.cm = collectionManager;
    }

    @Override
    public Response execute(Request request){
        // Формируем текст через методы CollectionManager
        String infoText = String.format(
                "Тип коллекции: %s%nДата инициализации: %s%nКоличество: %d",
                cm.getCollection().getClass().getSimpleName(),
                cm.getInitDate(),
                cm.getCollection().size()
        );
        return new Response(infoText);

    }

    @Override
    public String getName() {
        return "info";
    }
}
