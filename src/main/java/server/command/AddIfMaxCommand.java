package server.command;

import server.CollectionManager;
import shared.Request;
import shared.Response;
import shared.model.Organization;

import java.util.Comparator;
import java.util.Optional;

/**
 * Команда add_if_max: добавляет новый элемент в коллекцию,
 * если он превышает (по compareTo) максимальный.
 */
public class AddIfMaxCommand implements Command {
    private final CollectionManager cm;

    public AddIfMaxCommand(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Response execute(Request request) {
        // 1) извлекаем аргумент и проверяем тип
        Object arg = request.getArgument();
        if (!(arg instanceof Organization)) {
            return new Response("Ошибка: add_if_max требует аргумент типа Organization.");
        }
        Organization org = (Organization) arg;

        // 2) находим текущий максимум
        Optional<Organization> max = cm.getCollection().stream()
                .max(Comparator.naturalOrder());

        // 3) сравниваем и, при необходимости, добавляем
        if (max.isEmpty() || org.compareTo(max.get()) > 0) {
            cm.add(org);
            return new Response("Элемент добавлен (он больше максимального).");
        } else {
            return new Response("Элемент не добавлен (он не превышает максимальный).");
        }
    }

    @Override
    public String getName() {
        return "add_if_max";
    }
}
