package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Interactive UDP client for sending Requests to the server and receiving Responses.
 * Uses ConsoleManager and RequestSender to implement a REPL.
 */
public class Client {

    public static void main(String[] args) {
        // проверим, что нам передали хост и порт
        if (args.length != 2) {
            System.err.println("Usage: java client.Client <server-host> <server-port>");
            System.exit(1);
        }

        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Порт должен быть целым числом: " + args[1]);
            return;
        }

        try {
            InetAddress addr = InetAddress.getByName(host);
            // timeout 5 секунд, создаем сендер для отправки запросов
            RequestSender sender = new RequestSender(addr, port, 5000);
            ConsoleManager console = new ConsoleManager(sender);
            console.run();
        } catch (UnknownHostException e) {
            System.err.println("Не удалось найти хост: " + host);
        } catch (IOException e) {
            System.err.println("Ошибка клиентского приложения: " + e.getMessage());
        }
    }
}
