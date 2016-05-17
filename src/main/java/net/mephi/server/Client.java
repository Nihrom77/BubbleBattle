package net.mephi.server;

import net.mephi.client.components.Ball;
import net.mephi.client.components.Board;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Snoll on 11.05.2016.
 */
public class Client {
    private String clientName = "anon";
    private Socket socket = null;

    private Client thisClient = null;
    private DataOutputStream oos;
    private BufferedReader ois;
    private Ball ball = null;
    private String uuid;
    private Board board;
    private boolean isActive = true;
    public static final String DATA_REQUEST = "DATA_REQUEST";
    private Point cursor = new Point(0, 0);
    private Logger log = Logger.getLogger(MultipleSocketServer.class);

    public Client() {

    }

    public void setClient(Client c) {
        this.thisClient = c;

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
            @Override public void run() {

                try {
                    DataOutputStream outToServer = getOos();
                    BufferedReader inFromServer = getOis();
                    JSONParser parser = new JSONParser();
                    while (isActive) {


                        //Отправить свой id и новые координаты поля, относительно глобального
                        Point p = board.getCursorLocation();
                        ball.setCursorLocation(p);
                        ball.countNewFieldPosition(p);

                        JSONObject obj = new JSONObject();
                        obj.put("type", "cursor");
                        obj.put("uuid", getUUID());
                        obj.put("x", ball.getUserField().x);
                        obj.put("y", ball.getUserField().y);

                        log.debug("send " + obj.toString());
                        outToServer.write(obj.toString().getBytes());

                        try {
                            //ожидать обработанных данных
                            String res = inFromServer.readLine();
                            JSONObject o = (JSONObject) parser.parse(res);
                            log.debug("receive " + o.toString());
                            if (o.get("type").equals("refresh")) {
                                //перерисовать доску

                                board.refreshBoard(o, getUUID(), ball.getLinesShift(), thisClient);
                            }
                        } catch (ParseException e) {
                            log.error(e);
                        }
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                        }

                    }

                    JSONObject obj = new JSONObject();
                    obj.put("type", "shutdown");
                    obj.put("uuid", getUUID());
                    log.debug("send " + obj.toString());
                    outToServer.write(obj.toString().getBytes());

                    oos.close();
                    ois.close();
                    getSocket().close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        thread1.setName("Client main thread");
        thread1.start();

    }

    public DataOutputStream getOos() {
        return oos;
    }

    public void setOos(DataOutputStream oos) {
        this.oos = oos;
    }

    public BufferedReader getOis() {
        return ois;
    }

    public void setOis(BufferedReader ois) {
        this.ois = ois;
    }

    @Override public String toString() {
        return "<" + getUUID() + " / " + getBall().getName() + ">";
    }

}
