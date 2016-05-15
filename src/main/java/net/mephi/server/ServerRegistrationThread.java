package net.mephi.server;

import net.mephi.client.components.Ball;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

/**
 * Created by Snoll on 11.05.2016.
 */
public class ServerRegistrationThread implements Runnable {


    private MultipleSocketServer server = null;
    private Logger log = Logger.getLogger(MultipleSocketServer.class);

    public ServerRegistrationThread(MultipleSocketServer server) {
        this.server = server;
    }

    /**
     * Поток ожидания регистрации.
     * При подключении нового клиента, вносит его в список и ждет нового клиента.
     */
    @Override
    public void run() {
        try {
            ServerSocket socket1 = new ServerSocket(MultipleSocketServer.getPort());
            while (true) {


                log.debug("waiting new client...");
                Socket connection = socket1.accept();
                log.debug("Connected");
                InputStream is = connection.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);

                //читаем имя и цвет
                String returnStr = (String) ois.readObject();
                Ball b = new Ball(parseName(returnStr), Ball.START_CLIENT_RADIUS);
                b.setRandomPosition();
                b.setColor(getColor(returnStr));

                Client c = new Client(connection);
                c.setOis(ois);
                UUID uuid = UUID.randomUUID();
                c.setUUID(uuid.toString());
                c.setBall(b);
                //отправляем уникальный ID
                OutputStream os = connection.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                c.setOos(oos);
                oos.writeObject(c.getUUID());
                oos.flush();


                server.addClient(c);
                log.debug("registered new Client: "+c);

                //Если поток сервера спит(wait), то это его разюудит
                synchronized (server.lock) {
                    server.lock.notify();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * asd_Dsax_123_123_123 -> asb_Dsax
     *
     * @param input
     * @return
     */
    private static String parseName(String input) {
        String temp = input.substring(0, input.lastIndexOf("_"));
        temp = temp.substring(0, temp.lastIndexOf("_"));
        temp = temp.substring(0, temp.lastIndexOf("_"));
        return temp;
    }

    private Color getColor(String input) {
        String[] col = input.substring(parseName(input).length() + 1).split("_");
        return new Color(Integer.parseInt(col[0]), Integer.parseInt(col[1]), Integer.parseInt(col[2]));
    }
}
