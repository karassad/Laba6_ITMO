package server;

import shared.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Server {

    public static void main(String[] args) {
        //Серверный UDP-сокет запущен на этом порту
        final int PORT = 9854;

        try {

            //создаем экзепляр сокета
            DatagramSocket serverSocket = new DatagramSocket(PORT);

            //буфферы для хранения отправляемых и получаемых данных
            byte[] receivingDataBuffer = new byte[1024];
            byte[] sendingDataBuffer = new byte[1024];

            //экземпляр UPD-пакета для хранения клиентских данных с
            //с использованием буфера для получения данных
            DatagramPacket inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
            System.out.println("Waiting for a client to connect...");

            //save into buffer
            serverSocket.receive(inputPacket);
            System.out.println("Getting data, deserialization...");

            //десериализация
            ByteArrayInputStream byteStream = new ByteArrayInputStream(inputPacket.getData(), 0, inputPacket.getLength());
            ObjectInputStream is = new ObjectInputStream(byteStream);
            Message message = (Message) is.readObject();


            //print sent from client
//            String receiverData = new String(inputPacket.getData(), 0, inputPacket.getLength(), StandardCharsets.UTF_8);
            System.out.println("From client: " + message);

            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
