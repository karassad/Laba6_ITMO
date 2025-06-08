package server;

import server.command.*;
import shared.Request;
import shared.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dataBase.UserManager;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main server class.
 */
public class Server {
    // SLF4J-логгер (Logback)
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    //серверный UDP-порт
    static final int PORT = 9854;


    //объявление 2 пулов потоков
    //для обработки входящих клиентских запросов, без ограничения по количеству
    private static final ExecutorService requestProcessor = Executors.newCachedThreadPool();
    //обрабатывает отправку ответа клиенту
    private static final ExecutorService responseSender = Executors.newFixedThreadPool(4);


    public static void main(String[] args) {
        logger.info("Запуск сервера…");

        try {
//            String filename = System.getenv("COLLECTION_FILE"); // имя CSV-файла из окружения
//            if (filename == null) {
//                logger.error("Переменная окружения COLLECTION_FILE не установлена.");
//                System.exit(1);
//            } //???????

            //Загрузка коллекции
            CollectionManager collectionManager = new CollectionManager("");
            collectionManager.loadFromDatabase();
//            collectionManager.load();
//            logger.info("Коллекция загружена из '{}', размер={}",
//                    filename, collectionManager.getCollection().size());
            logger.info("Коллекция инициализирована. Работаем с PostgreSQL.");

            //Регистрируем shutdown-hook, чтобы при завершении JVM
            //всегда попытаться сохранить коллекцию:
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                logger.info("JVM завершает работу — сохраняем коллекцию…");
//                try {
//                    collectionManager.save();
//                } catch (Exception e) {
//                    logger.error("Ошибка при сохранении коллекции в shutdown hook: {}", e.getMessage(), e);
//                }
//            }));
//            Runtime.getRuntime().addShutdownHook(new Thread(() ->
//                    logger.info("JVM завершает работу — данные сохранены в PostgreSQL, дополнительное сохранение не требуется.")
//            ));

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


                    //пул занимает свободный поток и выполняет код
                    //обрабатываем байты от клиента отдельным потоком, чтобы не занимать основрной
                    //если нет свободных потоков - создает новый поток
                    requestProcessor.submit(() -> {
                        try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(inBytes))) {

                            //Десериализация Request
//                            Request request;
//                            try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(inBytes))) {
//                                request = (Request) is.readObject();
//                            }
                            Request request = (Request) is.readObject();
                            logger.debug("Десериализован Request: {}", request.getCommandName());

                            // Выполнение команды
                            //Response response = commandManager.execute(request);
                            Response response;
                            // Команды, которые можно выполнять без авторизации. тру\фолз или
                            boolean authFree = request.getCommandName().equals("register") || request.getCommandName().equals("login");
                            //если команла не требует авторизации или клиент прошел проверку
                            if (request.getCommandName().equals("register")) {
                                if (UserManager.register(request.getUsername(), request.getPassword())) {
                                    response = new Response("Регистрация успешна!");
                                } else {
                                    response = new Response("Ошибка: регистрация не удалась. Возможно, имя занято.");
                                }
                            } else if (request.getCommandName().equals("login")) {
                                if (UserManager.login(request.getUsername(), request.getPassword())) {
                                    response = new Response("Вход выполнен успешно.");
                                } else {
                                    response = new Response("Ошибка: неверный логин или пароль.");
                                }
                            } else if (authFree || UserManager.login(request.getUsername(), request.getPassword())) {
                                response = commandManager.execute(request);
                            } else {
                                response = new Response("Ошибка: вы не авторизованы. Команда не выполнена.");
                                logger.warn("Попытка выполнить команду без авторизации от пользователя '{}'", request.getUsername());
                            }

                            // Сериализация Response
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try (ObjectOutputStream os = new ObjectOutputStream(baos)) {
                                os.writeObject(response);
                            }
                            byte[] outBytes = baos.toByteArray();

                            responseSender.submit(() -> {
                                try {
                                    //оборачиваем в буффер
                                    ByteBuffer sendBuf = ByteBuffer.wrap(outBytes);
                                    channel.send(sendBuf, clientAddr);
                                    logger.info("Отправлен ответ клиенту {}", clientAddr);
                                } catch (IOException e) {
                                    logger.error("Ошибка при отправке ответа клиенту: {}", e.getMessage(), e);
                                }

//                            // Отправка ответа
//                            ByteBuffer sendBuf = ByteBuffer.wrap(outBytes);
//                            channel.send(sendBuf, clientAddr);
//                            logger.info("Отправлен ответ клиенту {}", clientAddr);
                            });
                        } catch (IOException | ClassNotFoundException e) {
                            logger.error("Ошибка обработки запроса: {}", e.getMessage(), e);
                        }
                    });

                } catch (IOException e) {
                    logger.error("Ошибка сервера: {}", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}




