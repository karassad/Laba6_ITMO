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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

/**
 * Class for handling user input and command creation.
 */
public class ConsoleManager {
    private final Scanner scanner = new Scanner(System.in);
    private final RequestSender requestSender;
    //используем интерфейс двусторонней очереди, реализуем через массив
    private final Deque<String> history = new ArrayDeque<>();

    public ConsoleManager(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    public void run() {
        System.out.println("Введите команду (help для списка команд):");
        while (true) {
            //Приглашение без перехода на новую строку
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            //Разбор команды и аргументов
            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0];
            //тернарный оператор
            String args = parts.length > 1 ? parts[1].trim() : "";

            //сохраняем имя команды в историю (не для history и не для exit)
            if (!cmd.equals("history") && !cmd.equals("exit")) {
                history.addFirst(cmd);
                if (history.size() > 8) history.removeLast();
            }

            //обрабатываем history
            if (cmd.equals("history")) {
                if (history.isEmpty()) {
                    System.out.println("История пустая.");
                } else {
                    System.out.println("Последние команды: " + String.join(", ", history));
                }
                continue;
            }

            try {
                //Строим Request; null — сигнал exit
                Request req = buildRequest(cmd, args);
                if (req == null) break;

                //Отправляем и печатаем ответ
                Response resp = requestSender.send(req);
                System.out.println(resp.getResponseText());

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: неверный формат числа. Попробуйте ещё раз.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Ошибка при общении с сервером: " + e.getMessage());
            }
        }
        System.out.println("Клиент завершил работу.");
    }
    private Request buildRequest(String cmd, String args) {
        switch (cmd) {
            //Однострочные, без аргументов
            case "help":
            case "info":
            case "show":
            case "clear":
            case "print_ascending":
            case "min_by_creation_date":
                return new Request(cmd, null);

            case "exit":
                System.out.println("Завершаем работу без сохранения.");
                return null;

            //Inline-аргументы
            case "remove_by_id":
                if (args.isEmpty()) {
                    System.out.println("Ошибка: команда remove_by_id требует аргумент id.");
                    return new Request("help", null);
                }
                return new Request(cmd, Integer.parseInt(args));

            case "filter_less_than_official_address":
                if (args.isEmpty()) {
                    System.out.println("Ошибка: требуется zipCode.");
                    return new Request("help", null);
                }
                return new Request(cmd, args);

            case "filter_greater_than_type":
                if (args.isEmpty()) {
                    System.out.println("Ошибка: требуется тип (e.g. COMMERCIAL).");
                    return new Request("help", null);
                }
                try {
                    return new Request(cmd, OrganizationType.valueOf(args.trim().toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    System.out.println("Неверный тип организации. Повторите ввод.");
                    return new Request("help", null);
                }

            case "execute_script":
                if (args.isEmpty()) {
                    System.out.println("Ошибка: команда execute_script требует аргумент.");
                    return new Request("help", null);
                }
                return new Request(cmd, args);

            //нужен объект Organization
            case "add":
            case "add_if_min":
            case "add_if_max":
            case "remove_lower":
            case "remove_greater":
                return new Request(cmd, readOrganization());

            case "update":
                if (args.isEmpty()) {
                    System.out.println("Ошибка: после update должен идти числовой id.");
                    return new Request("help", null);
                }
                int id = Integer.parseInt(args);
                System.out.println("Введите новые данные для организации с id=" + id + ":");
                Organization newOrg = readOrganization();
                return new Request("update", new Object[]{id, newOrg});

            default:
                System.out.println("Неизвестная команда. Введите help.");
                return new Request("help", null);
        }
    }


    private Organization readOrganization() {
        String name;
        //Ввод и проверка name (не null, не пустая строка)
        while (true) {
            System.out.print("Введите имя (не пустое): ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Ошибка: имя не может быть пустым. Повторите ввод.");
            } else {
                break;
            }
        }

        Float x;
        //Ввод и проверка координаты X (Float, не null)
        while (true) {
            System.out.print("Введите координату X (число): ");
            String line = scanner.nextLine().trim();
            try {
                x = Float.parseFloat(line);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число. Повторите ввод.");
            }
        }

        Integer y;
        // Ввод и проверка координаты Y (int, ≤132)
        while (true) {
            System.out.print("Введите координату Y (целое ≤132): ");
            String line = scanner.nextLine().trim();
            try {
                y = Integer.parseInt(line);
                if (y > 132) {
                    System.out.println("Ошибка: Y не может быть больше 132. Повторите ввод.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число. Повторите ввод.");
            }
        }

        Float turnover;
        // Ввод и проверка annualTurnover (Float, >0)
        while (true) {
            System.out.print("Введите годовой оборот (число > 0): ");
            String line = scanner.nextLine().trim();
            try {
                turnover = Float.parseFloat(line);
                if (turnover <= 0) {
                    System.out.println("Ошибка: оборот должен быть положительным. Повторите ввод.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число. Повторите ввод.");
            }
        }

        OrganizationType type = null;
        // Ввод и проверка enum OrganizationType (с учётом регистра)
        while (type == null) {
            System.out.print("Введите тип организации из списка: ");
            for (OrganizationType t : OrganizationType.values()) {
                System.out.print(t.name() + " ");
            }
            System.out.println();

            String line = scanner.nextLine().trim().toUpperCase();
            if (line.isEmpty()) {
                // допускаем null, если поле может быть пустым
                break;
            }
            try {
                type = OrganizationType.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: неверный тип. Повторите ввод.");
            }
        }

        String zip;
        //Ввод и проверка zipCode (String длиной ≤28 или null)
        while (true) {
            System.out.print("Введите почтовый индекс (макс. 28 символов) или пустую строку: ");
            zip = scanner.nextLine().trim();
            if (zip.isEmpty()) {
                zip = null;
                break;
            } else if (zip.length() > 28) {
                System.out.println("Ошибка: длина индекса не должна превышать 28 символов. Повторите ввод.");
            } else {
                break;
            }
        }

        Address address = (zip == null) ? null : new Address(zip);
        Coordinates coords = new Coordinates(x, y);

        //Собираем и возвращаем новый объект
        return new Organization(name, coords, turnover, type, address);
    }

}
