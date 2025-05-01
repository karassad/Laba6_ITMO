package client;

import shared.Request;
import shared.Response;
import shared.model.Address;
import shared.model.Coordinates;
import shared.model.Organization;
import shared.model.OrganizationType;
import shared.model.Coordinates;

import javax.management.relation.RelationServiceNotRegisteredException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Class for handling user input and command creation.
 */
public class ConsoleManager {

    private final Scanner scanner = new Scanner(System.in);
    private final RequestSender requestSender;

    public ConsoleManager(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    public void run(){
        System.out.println("Введите команду (help для списка команд):");
        while (true){
            System.out.println("> ");
            String line = scanner.nextLine().trim(); //считываем строку до конца
            if (line.isEmpty()) continue;

            String[] parts_input = line.split("\\s+", 2); //делим по пробелам
            String cmdPart = parts_input[0];
            String argsPart = parts_input.length > 1 ? parts_input[1].trim() : ""; //тернарный оператор

            try{
                Request req = buildRequest(cmdPart, argsPart);
                if (req == null ) break; //exit
                Response response = requestSender.send(req);
                System.out.println(response.getResponseText());
                if ("exit".equalsIgnoreCase(cmdPart)) break;

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private Request buildRequest(String cmd, String args){

        switch (cmd) {
            // команды без аргументов
            case "help":
            case "info":
            case "show":
            case "print_ascending":
            case "clear":
            case "exit":
                return new Request(cmd, null);

            // команды с одним числовым аргументом
            case "remove_by_id": {
                int id = Integer.parseInt(args);
                return new Request(cmd, id);
            }

            // команды с одной сущностью
            case "add":
            case "add_if_min":
            case "add_if_max": {
                Organization org = readOrganization();
                return new Request(cmd, org);
            }

            // TODO: остальные команды: update, clear, add_if_min и т.д.

            default:
                System.out.println("Неизвестная команда. Введите help.");
                return new Request("help", null);
        }
    }

    /**
     * Запрашивает у пользователя данные для создания нового {@link Organization}.
     *
     * @return объект Organization с введёнными полями
     */
    private Organization readOrganization(){
        System.out.print("Введите имя: ");
        String name = scanner.nextLine().trim();

        System.out.print("Введите координату X: ");
        float x = Float.parseFloat(scanner.nextLine().trim());

        System.out.print("Введите координату Y: ");
        int y = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Введите годовой оборот: ");
        float turnover = Float.parseFloat(scanner.nextLine().trim());

        System.out.print("Введите тип организации из списка: ");
        for (OrganizationType t : OrganizationType.values()) {
            System.out.print(t + " ");
        }
        System.out.println();
        OrganizationType type = OrganizationType.valueOf(scanner.nextLine().trim());

        System.out.print("Введите почтовый индекс (или пустую строку): ");
        String zip = scanner.nextLine().trim();
        Address address = zip.isEmpty() ? null : new Address(zip);

        return new Organization(name, new Coordinates(x, y), turnover, type, address);
    }
}
