package client;

import shared.Request;
import shared.Response;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;


/**
 * Класс для отправки запросов на сервер и получения ответов по протоколу UDP.
 * Выполняет сериализацию объектов и десериализацию объектов.
 * Настраивает таймаут ожидания ответа
 */
public class  RequestSender {
    private final InetAddress serverAddress;
    private final int serverPort;
    private final int timeoutMs;

    public RequestSender(InetAddress serverAddress, int serverPort, int timeoutMs) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.timeoutMs = timeoutMs;
    }


    /**
     * Отправляет Request на сервер и ждёт Response.
     * @throws SocketTimeoutException если сервер не ответил за timeoutMs миллисекунд
     */
    public Response send(Request request) throws IOException, ClassNotFoundException {
        // Сериализация Request в байты
        byte[] requestBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(request);
            oos.flush(); //проверяет, что все файлы точно вышли из буфферов oos в baos
            requestBytes = baos.toByteArray();
        }
        
        //отправка
        //создаем upd-сокет
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(timeoutMs); //выбросит исключение, если по истечению времени не придет байтов
        DatagramPacket sendPacket = new DatagramPacket(requestBytes, requestBytes.length, serverAddress, serverPort);
        socket.send(sendPacket);

        // Приём ответа
        byte[] buf = new byte[4096]; //!!!!!!!!
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        socket.receive(receivePacket);
        socket.close();

        // Десериализация Response
        try(ObjectInputStream ois = new ObjectInputStream (new ByteArrayInputStream
                        (receivePacket.getData(),  //сам весь большой буффер
                                0,  //откуда ничанаем читать
                                receivePacket.getLength() //сколько байт в буфере действительно содержит полезную нагрузку
                        ))
        ){
            return (Response) ois.readObject();
        }


    }
}

