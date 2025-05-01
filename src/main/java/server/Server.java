package server;

import server.command.*;
import shared.Message;
import shared.Request;
import shared.Response;

//import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.logging.ConsoleHandler;
//import java.util.logging.FileHandler;
//import java.util.logging.SimpleFormatter; //логирвоание

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * Main server class.
 */
public class Server {
    // получаем логгер по имени класса
//    private static final Logger logger = Logger.getLogger(Server.class.getName());
    public static void main(String[] args) {


        //Серверный UDP-сокет запущен на этом порту
        final int PORT = 9854;

        try {

            String filename = System.getenv("COLLECTION_FILE"); //переменная окружения для передачи имени файла
            if (filename==null){
                System.err.println("Переменная окружения COLLECTION_FILE не установлена.");
                System.exit(1);
            }

            CollectionManager collectionManager = new CollectionManager(filename); //передаем в менеджер файл для парсинга
            collectionManager.load();    //парсинг

            CommandManager commandManager = new CommandManager(
                    (Command) new HelpCommand(),
                    (Command) new InfoCommand(collectionManager),
                    (Command) new ShowCommand(collectionManager),
                    (Command) new AddCommand(collectionManager),
                    (Command) new ClearCommand(collectionManager) //ДОРЕАЛИЗОВАТЬ
            );


            //сетевой канал в неблокирующем режиме
//           DatagramSocket serverSocket = new DatagramSocket(PORT);
            DatagramChannel channel = DatagramChannel.open(); //открыли канал и подключили к порту
            channel.bind(new InetSocketAddress(PORT));
            channel.configureBlocking(false); //переключили на неблокирующий режим
            System.out.println("Сервер запущен на порту "+ PORT);

            ByteBuffer recvBuf = ByteBuffer.allocate(4096); //receiveBuffer (4096 байт)

//            //буфферы для хранения отправляемых и получаемых данных
//            byte[] receivingDataBuffer = new byte[1024];
//            byte[] sendingDataBuffer = new byte[1024];

            while(true){
                try{ //Основной цикл обработки запросов

                    recvBuf.clear();
                    SocketAddress clientAddr = channel.receive(recvBuf); //записываем в буффер инфу + сохраняем в
                    //clientAddr адрес и порт отправителя

                    // если null — данных нет, сразу переходим к следующей итерации
                    if (clientAddr == null) {
                        Thread.yield();
                        continue;
                    }

                    //подготовка буффера к чтению
                    recvBuf.flip();
                    byte[] inBytes = new byte[recvBuf.remaining()]; //Выделяем ровно столько массива, сколько байт пришло
                    recvBuf.get(inBytes); //чистает байты из буфера

                    //Десериализация Request
                    Request request;
                    try(ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(inBytes))){
                        request = (Request) is.readObject();
                    }

                    Response response = commandManager.execute(request); //отправляем в менеджер команду

                    //Сериализация Response
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try(ObjectOutputStream os = new ObjectOutputStream(baos)){
                        os.writeObject(response); //создаем буфеер для байтов, упаковываем его в поток, в этот поток записываем нащ ответ
                    }

                    byte[] outBytes  = baos.toByteArray(); //записываем просто байты

                    //отправка клиенту
                    ByteBuffer sendBuf = ByteBuffer.wrap(outBytes); //оборачиваем в буффер сообщение
                    channel.send(sendBuf, clientAddr);

//                    //экземпляр UPD-пакета для хранения клиентских данных с
//                    //с использованием буфера для получения данных
//                    DatagramPacket inputPacket = new DatagramPacket(recvBuf, ;
//                    System.out.println("Waiting for a client to connect...");
//
//                    //save into buffer
//                    channel.receive(inputPacket);
//                    System.out.println("Getting data, deserialization...");
//
//                    //десериализация (Преобразование байтов из пакета обратно в объект Request.)
//                    ByteArrayInputStream byteStream = new ByteArrayInputStream(inputPacket.getData(), 0, inputPacket.getLength());
//                    ObjectInputStream is = new ObjectInputStream(byteStream);
//                    Request request = (Request) is.readObject();
////                Message message = (Message) is.readObject();
//
//                    //выполнение команды
//                    Response response = new Response(request);
//
//                    //Сериализация и отправка ответа
//                    //Преобразование объекта Response в массив байтов.
//                    recvBuf.clear();
//                    ByteArrayOutputStream responseByteStream = new ByteArrayOutputStream();
//                    ObjectOutputStream os = new ObjectOutputStream(responseByteStream);
//                    os.writeObject(response);
//                    os.flush();
//                    sendingDataBuffer = responseByteStream.toByteArray();
//                    //Отправка ответа клиенту на его IP и порт.
//                    InetAddress clientAddress = inputPacket.getAddress(); //получаем Ip адресс клиента
//                    int clientPort = inputPacket.getPort();//получаем порт клиента
//                    DatagramPacket outputPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer., clientAddress, clientPort);
//                    serverSocket.send(outputPacket);

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Ошибка обработки запроса: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

//        // 1) Консольный хэндлер
//        ConsoleHandler ch = new ConsoleHandler();
//        ch.setLevel(Level.INFO);
//        ch.setFormatter(new SimpleFormatter());
//        logger.addHandler(ch); //прикрепляем к логгеру
//
//        // 2) Файловый хэндлер
//        FileHandler fh = new FileHandler("server.log", true);
//        fh.setLevel(Level.FINE);                // хотим видеть и FINE (аналог DEBUG)
//        fh.setFormatter(new SimpleFormatter());
//        logger.addHandler(fh);//прикрепляем к логгеру
//
//        // устанавливаем корневой уровень логгера
//        logger.setLevel(Level.FINE);
//    }
}

