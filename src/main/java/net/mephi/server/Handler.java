package net.mephi.server;

import net.mephi.client.components.Ball;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.UUID;

/**
 * Created by Snoll on 15.05.2016.
 */
public class Handler implements Runnable {
    private final SocketChannel _socketChannel;
    private final SelectionKey _selectionKey;

    private static final int READ_BUF_SIZE = 1024;
    private static final int WRiTE_BUF_SIZE = 1024;
    private ByteBuffer _readBuf = ByteBuffer.allocate(READ_BUF_SIZE);
    private ByteBuffer _writeBuf = ByteBuffer.allocate(WRiTE_BUF_SIZE);
    private Logger log = Logger.getLogger(Handler.class);
    private CheckCollissions col;


    public Handler(CheckCollissions col, Selector selector, SocketChannel socketChannel)
        throws IOException {
        this.col = col;

        _socketChannel = socketChannel;
        _socketChannel.configureBlocking(false);

        // Register _socketChannel with _selector listening on OP_READ events.
        // Callback: Handler, selected when the connection is established and ready for READ
        _selectionKey = _socketChannel.register(selector, SelectionKey.OP_READ);
        _selectionKey.attach(this);
        selector.wakeup(); // let blocking select() return
    }

    public void run() {
        try {
            if (_selectionKey.isReadable()) {
                read();
            } else if (_selectionKey.isWritable()) {
                write();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Process data by echoing input to output
    synchronized void process() {
        _readBuf.flip();
        byte[] bytes = new byte[_readBuf.remaining()];
        _readBuf.get(bytes, 0, bytes.length);

        try {
            String s = new String(bytes);
            JSONParser parser = new JSONParser();
            JSONObject o = (JSONObject) parser.parse(s);
            log.debug("received " + o.toString());
            if (o.get("type").equals("register")) {
                log.debug("New client connected");

                //читаем имя, цвет и размер экрана
                Ball b = new Ball((String) o.get("name"), Ball.START_CLIENT_RADIUS);
                //
                b.getUserField().width = ((Long) o.get("width")).intValue();
                b.getUserField().height = ((Long) o.get("height")).intValue();
                JSONObject color = (JSONObject) o.get("color");
                b.setColor(new Color(((Long) color.get("red")).intValue(),
                    ((Long) color.get("green")).intValue(), ((Long) color.get("blue")).intValue()));
                b.setRandomFieldPosition();


                Client c = new Client(null);
                UUID uuid = UUID.randomUUID();
                c.setUUID(uuid.toString());
                c.setBall(b);

                //отправляем уникальный ID  и глобальные координаты поля
                JSONObject answer = new JSONObject();
                answer.put("type", "register_complete");
                answer.put("uuid", c.getUUID());
                answer.put("field.x", b.getUserField().x);
                answer.put("field.y", b.getUserField().y);
                String resp = answer.toString() + '\n';

                log.debug("send " + answer.toString());
                _writeBuf = ByteBuffer.wrap(resp.getBytes());


                col.addClient(c);
                log.debug("registered new Client: " + c);

            } else if (o.get("type").equals("cursor")) {
                //Пришли новые координаты поля
                col.updateClient((String) o.get("uuid"),
                    new Point(((Long) o.get("x")).intValue(), ((Long) o.get("y")).intValue()));
                //отправляем все объекты
                JSONObject answer = getAllResponceData((String) o.get("uuid"));
                String resp = answer.toString() + '\n';

                log.debug("send " + resp);
                _writeBuf = ByteBuffer.wrap(resp.getBytes());
            }
        } catch (ParseException p) {
        }



        // Set the key's interest to WRITE operation
        _selectionKey.interestOps(SelectionKey.OP_WRITE);
        _selectionKey.selector().wakeup();
    }

    synchronized void read() throws IOException {
        try {
            int numBytes = _socketChannel.read(_readBuf);

            //            System.out.println("read(): #bytes read into '_readBuf' buffer = " + numBytes);
            if (numBytes == -1) {
                _selectionKey.cancel();
                _socketChannel.close();
                log.debug("read(): client connection might have been dropped!");
            } else {
                MultipleSocketServer.getWorkerPool().execute(() -> process());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            _selectionKey.cancel();
            _selectionKey.channel().close();
            return;
        }
    }

    void write() throws IOException {
        int numBytes = 0;

        try {
            numBytes = _socketChannel.write(_writeBuf);
            //            System.out.println("write(): #bytes read from '_writeBuf' buffer = " + numBytes);
            if (numBytes > 0) {
                _readBuf.clear();
                _writeBuf.clear();

                // Set the key's interest-set back to READ operation
                _selectionKey.interestOps(SelectionKey.OP_READ);
                _selectionKey.selector().wakeup();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            _selectionKey.cancel();
            _socketChannel.close();
        }
    }

    /**
     * Пересылаем еду и клиентов, в пределах видимости клиента
     *
     * @param uuid id клиента, которому будут отправлены данные
     * @return Объект пересылки
     */
    private JSONObject getAllResponceData(String uuid) {
        List<Client> list = col.getClientsList();
        Client responceTo = null;
        for (Client c : list) {
            if (c.getUUID().equals(uuid)) {
                responceTo = c;
                break;
            }
        }


        Ball[] food = col.getFoodList();
        JSONObject response = new JSONObject();
        response.put("type", "refresh");
        JSONArray arrayFood = new JSONArray();
        for (Ball b : food) {
            if (b.isVisible() && responceTo.getBall().isBallInCurrentField(b)) {
                JSONObject foodCoord = new JSONObject();
                foodCoord
                    .put("x", b.getCenter().x - responceTo.getBall().getLeftTopFieldPosition().x);
                foodCoord
                    .put("y", b.getCenter().y - responceTo.getBall().getLeftTopFieldPosition().y);

                JSONObject color = new JSONObject();
                color.put("red", b.getColor().getRed());
                color.put("green", b.getColor().getGreen());
                color.put("blue", b.getColor().getBlue());
                foodCoord.put("color", color);

                arrayFood.add(foodCoord);
            }

        }
        response.put("food", arrayFood);

        JSONArray arrayClient = new JSONArray();
        for (Client c : list) {
            if (c.getBall().isVisible() && responceTo.getBall().isBallInCurrentField(c.getBall())) {
                JSONObject balls = new JSONObject();
                balls.put("x", c.getBall().getCenterGlobalPosition().x - responceTo.getBall()
                    .getLeftTopFieldPosition().x);
                balls.put("y", c.getBall().getCenterGlobalPosition().y - responceTo.getBall()
                    .getLeftTopFieldPosition().y);
                balls.put("uuid", c.getUUID());
                balls.put("radius", c.getBall().getRadius());
                balls.put("name", c.getBall().getName());

                JSONObject color = new JSONObject();
                color.put("red", c.getBall().getColor().getRed());
                color.put("green", c.getBall().getColor().getGreen());
                color.put("blue", c.getBall().getColor().getBlue());
                balls.put("color", color);
                arrayClient.add(balls);
            }
        }
        response.put("clients", arrayClient);

        return response;
    }

}
