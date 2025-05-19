package server;

import server.command.*;
import shared.Request;
import shared.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.logging.ConsoleHandler;
//import java.util.logging.FileHandler;
//import java.util.logging.SimpleFormatter; //логгирование

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

/**
 * Main server class.
 */
public class Server {
    // SLF4J-логгер (Logback)
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        logger.info("Запуск сервера…");

        //серверный UDP-порт
        final int PORT = 9854;

        try {
            String filename = System.getenv("COLLECTION_FILE"); // имя CSV-файла из окружения
            if (filename == null) {
                logger.error("Переменная окружения COLLECTION_FILE не установлена.");
                System.exit(1);
            }

            //Загрузка коллекции
            CollectionManager collectionManager = new CollectionManager(filename);
            collectionManager.load();
            logger.info("Коллекция загружена из '{}', размер={}",
                    filename, collectionManager.getCollection().size());

            //Регистрируем shutdown-hook, чтобы при завершении JVM
            //всегда попытаться сохранить коллекцию:
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("JVM завершает работу — сохраняем коллекцию…");
                try {
                    collectionManager.save();
                } catch (Exception e) {
                    logger.error("Ошибка при сохранении коллекции в shutdown hook: {}", e.getMessage(), e);
                }
            }));

            //Регистрация команд
            CommandManager commandManager = new CommandManager(
                    new HelpCommand(),
                    new InfoCommand(collectionManager),
                    new ShowCommand(collectionManager),
                    new AddCommand(collectionManager),
                    new ClearCommand(collectionManager),
                    new RemoveByIdCommand(collectionManager),
                    new UpdateCommand(collectionManager),
                    new SaveCommand(collectionManager),
                    new PrintAscendingCommand(collectionManager),
                    new AddIfMaxCommand(collectionManager),
                    new RemoveGreaterCommand(collectionManager),
                    new RemoveLowerCommand(collectionManager),
                    new MinByCreationDateCommand(collectionManager),
                    new FilterLessThanOfficialAddressCommand(collectionManager),
                    new FilterGreaterThanTypeCommand(collectionManager)
            );
            commandManager.register(new ExecuteScriptCommand(commandManager));
            logger.info("Все команды зарегистрированы.");

            // Открытие канала в неблокирующем режиме
            DatagramChannel channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(PORT));
            channel.configureBlocking(false);
            logger.info("Сервер запущен на порту {}", PORT);

            ByteBuffer recvBuf = ByteBuffer.allocate(4096);
            while (true) {
                try {
                    recvBuf.clear();
                    SocketAddress clientAddr = channel.receive(recvBuf);
                    if (clientAddr == null) {
                        Thread.yield();
                        continue;
                    }

                    logger.info("Получен пакет от {}", clientAddr);

                    //Переводим буфер recvBuf из режима записи в режим чтения
                    recvBuf.flip();
                    //Копируем содержимое буфера в массив байт, создаем буффер размера limit - position
                    byte[] inBytes = new byte[recvBuf.remaining()];
                    recvBuf.get(inBytes);

                    //Десериализация Request
                    Request request;
                    try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(inBytes))) {
                        request = (Request) is.readObject();
                    }
                    logger.debug("Десериализован Request: {}", request.getCommandName());

                    // Выполнение команды
                    Response response = commandManager.execute(request);

                    // Сериализация Response
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (ObjectOutputStream os = new ObjectOutputStream(baos)) {
                        os.writeObject(response);
                    }
                    byte[] outBytes = baos.toByteArray();

                    // Отправка ответа
                    ByteBuffer sendBuf = ByteBuffer.wrap(outBytes);
                    channel.send(sendBuf, clientAddr);
                    logger.info("Отправлен ответ клиенту {}", clientAddr);

                } catch (IOException | ClassNotFoundException e) {
                    logger.error("Ошибка обработки запроса: {}", e.getMessage(), e);
                }
            }

        } catch (IOException e) {
            logger.error("Ошибка сервера: {}", e.getMessage(), e);
        }
    }
}
