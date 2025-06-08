package server.command;

import server.CollectionManager;
import server.CommandManager;
import shared.Request;
import shared.Response;
import shared.model.Address;
import shared.model.Coordinates;
import shared.model.Organization;
import shared.model.OrganizationType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Команда execute_script: читает команды из файла и выполняет их по очереди.
 * Защищает от зацикливания (рекурсии скриптов).
 */
public class ExecuteScriptCommand implements Command {
    private final CommandManager cmdMgr;
    // держим стек имён файлов, чтобы не заходить рекурсивно
    private final Set<String> scriptStack = new HashSet<>();

    public ExecuteScriptCommand(CommandManager cmdMgr) {
        this.cmdMgr = cmdMgr;
    }

    @Override
    public Response execute(Request request) {
        String filename = (String) request.getArgument();
        if (scriptStack.contains(filename)) {
            return new Response("Рекурсия скриптов обнаружена: " + filename);
        }
        scriptStack.add(filename);

        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
//            while ((line = reader.readLine()) != null) {
//                line = line.trim();
//                if (line.isEmpty() || line.startsWith("#")) continue;  // пропускаем пустые и комменты
//                out.append("> ").append(line).append("\n");
//                Request inner = Request.parse(line);
//                Response r = cmdMgr.execute(inner);
//                out.append(r.getResponseText()).append("\n");
//            }
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                out.append("> ").append(line).append("\n");

                String[] parts = line.split("\\s+", 2);
                String cmd = parts[0];
                String arg = parts.length > 1 ? parts[1] : "";

                Response resp;

                switch (cmd) {
                    case "add":
                    case "add_if_max":
                    case "remove_lower":
                    case "remove_greater": {
                        // скрипт должен сразу содержать 6 строчек с полями
                        Organization org = readOrgFromScript(reader);
                        resp = cmdMgr.execute(new Request(cmd, arg, request.getUsername(), request.getPassword()));
                        break;
                    }
                    case "update": {
                        // строка: "update 123"
                        int id = Integer.parseInt(arg);
                        // следующие 6 строчек — новое состояние
                        Organization newOrg = readOrgFromScript(reader);
                        resp = cmdMgr.execute(new Request("update", new Object[]{id, newOrg}, request.getUsername(), request.getPassword()));

                        break;
                    }
                    default: {
                        // всё остальное (info, show, remove_by_id, execute_script вложенный...)
                        Object argument = arg.isEmpty() ? null : arg;
                        resp = cmdMgr.execute(new Request(cmd, argument, request.getUsername(), request.getPassword()));

                    }
                }

                out.append(resp.getResponseText()).append("\n");
            }
        } catch (Exception e) {
            return new Response("Ошибка при выполнении скрипта: " + e.getMessage());
        } finally {
            scriptStack.remove(filename);
        }
        return new Response(out.toString());
    }

    @Override
    public String getName() {
        return "execute_script";
    }

    private Organization readOrgFromScript(BufferedReader reader) throws IOException {
        // 1) name
        String name = reader.readLine();
        // 2) x (Float)
        float x = Float.parseFloat(reader.readLine());
        // 3) y (int)
        int y = Integer.parseInt(reader.readLine());
        // 4) annualTurnover
        float turnover = Float.parseFloat(reader.readLine());
        // 5) type (может быть пустой строкой → null)
        String typeLine = reader.readLine();
        OrganizationType type = typeLine.isEmpty()
                ? null
                : OrganizationType.valueOf(typeLine.trim().toUpperCase());
        // 6) zipCode (может быть пустой)
        String zip = reader.readLine();
        Address addr = zip.isEmpty() ? null : new Address(zip);

        return new Organization(name, new Coordinates(x, y), turnover, type, addr);
    }

}
