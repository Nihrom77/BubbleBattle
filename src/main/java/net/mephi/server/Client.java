package net.mephi.server;

import net.mephi.client.Clients;
import net.mephi.client.components.Ball;
import net.mephi.client.components.Board;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Snoll on 11.05.2016.
 */
public class Client {
    private String clientName = "anon";
    private Socket socket = null;


    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Ball ball = null;
    private String uuid;
    private Board board;
    private boolean isActive = true;
    public static final String DATA_REQUEST = "DATA_REQUEST";
    private Point cursor = new Point(0, 0);
    private Logger log = Logger.getLogger(MultipleSocketServer.class);

    public Client() {

    }

    public Client(Socket s) {
        this.socket = s;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID() {
        return uuid;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }

    public Ball getBall() {
        return ball;
    }

    public Point getCursorPosition() {
        return cursor;
    }

    public void setCursorPosition(Point cursor) {
        this.cursor = cursor;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setBoard(Board b) {
        this.board = b;
    }

    public void endGame() {
        this.isActive = false;
    }

    public void startGame() {
        //Запуск потока для обмена сообщениями с сервером
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    ObjectInputStream ois = getOis();
                    ObjectOutputStream oos = getOos();

                    while (isActive) {
                        String read = (String) ois.readObject();
                        if (read.equals(Client.DATA_REQUEST)) {

                            //Отправить свой id и координаты мыши


                            Point p = board.getCursorLocation();
                            log.debug("send "+this + " / " + p.x + "_" + p.y);
                            oos.writeObject(getUUID() + "/" + p.x + "_" + p.y);
                            oos.flush();
                            oos.reset();
                            //ожидать обработанных данных

                            Clients clients = (Clients) ois.readObject();

                            //перерисовать доску
                            log.debug("Refresh board"+clients.getClientBalls());
                            board.refreshBoard(clients, getUUID());
                        }
                    }
                    oos.close();
                    ois.close();
                    getSocket().close();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        thread1.start();

    }
    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    @Override
    public String toString() {
        return "<"+getUUID() + " / " + getBall().getName()+">";
    }

}
