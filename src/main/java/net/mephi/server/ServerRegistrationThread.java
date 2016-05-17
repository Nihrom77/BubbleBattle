package net.mephi.server;

import net.mephi.client.components.Ball;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
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
    @Override public void run() {
        try (InputStream input = new FileInputStream("classes/connections.properties")) {
            Properties p = new Properties();
            ServerSocket socket1 = new ServerSocket(Integer.parseInt(p.getProperty("port")));
            while (true) {


                log.debug("waiting new client...");
                Socket connection = socket1.accept();
                log.debug("Connected");
                InputStream is = connection.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);

                //читаем имя, цвет и размер экрана
                Ball b = (Ball) ois.readObject();
                b.setRandomFieldPosition();
                Client c = new Client(connection);
                //                c.setOis(ois);
                UUID uuid = UUID.randomUUID();
                c.setUUID(uuid.toString());
                c.setBall(b);

                //отправляем уникальный ID
                OutputStream os = connection.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                //                c.setOos(oos);
                oos.writeObject(c.getUUID());
                oos.flush();


                server.addClient(c);
                log.debug("registered new Client: " + c);

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



}
