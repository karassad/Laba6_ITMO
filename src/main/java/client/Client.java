package client;

import shared.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {

    public static void main(String[] args) {

        //порт к которому будем подключаться
        final int PORT = 9854;
        try{
            //создаем экземпляр сокета для клиента,
            //не привязываем к определенному порту
            DatagramSocket clientSocket = new DatagramSocket();

            //получаем IP адрес сервера
            InetAddress IPAddress = InetAddress.getByName("localhost");

            byte[] receivingDataBuffer = new byte[1024];
            byte[] sendingDataBuffer = new byte[1024];

            String sent = "meow meow";
            Message message = new Message("hello you");

            //сериализуем передаваемое сообщение т.к udp работает не \
            //с объектами а с массивом байт

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();//временный поток для хранения байт
            ObjectOutputStream os = new ObjectOutputStream(byteStream);//объект, которые сериализует и передает в поток
            os.writeObject(message);
            os.flush();
//            sendingDataBuffer = sent.getBytes(StandardCharsets.UTF_8);

            //создаем UDP пакет
            DatagramPacket sendingDataPacket = new DatagramPacket(byteStream.toByteArray(), byteStream.size(), IPAddress, PORT);

            //send packet to serv
            clientSocket.send(sendingDataPacket);

            clientSocket.close();




        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
