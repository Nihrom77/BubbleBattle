package net.mephi.server;

import net.mephi.client.Clients;
import net.mephi.client.components.Ball;
import net.mephi.client.components.Board;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultipleSocketServer implements Runnable {

    private final Selector _selector;
    private final ServerSocketChannel _serverSocketChannel;
    private static final int WORKER_POOL_SIZE = 10;
    private static ExecutorService _workerPool;


    private List<Client> clientsList = new ArrayList<>();
    private List<Client> clientList4Delete = new ArrayList<>();
    private Logger log = Logger.getLogger(MultipleSocketServer.class);
    private CheckCollissions col;


    private List<Client> clientList4Register = new ArrayList<>();
    private Ball[] foods = new Ball[Board.MAX_FOOD_AMOUNT];
    public Object lock = new Object();


    public MultipleSocketServer(int port) throws IOException {
        CheckCollissions col = new CheckCollissions();
        Thread t = new Thread(col);
        t.setName("check collissions thread");
        t.start();

        _selector = Selector.open();
        _serverSocketChannel = ServerSocketChannel.open();
        _serverSocketChannel.socket().bind(new InetSocketAddress(port));
        _serverSocketChannel.configureBlocking(false);

        // Register _serverSocketChannel with _selector listening on OP_ACCEPT events.
        // Callback: Acceptor, selected when a new connection incomes.
        //Регистрируем сокет-канал, который слушает OP_ACCEPT события
        //При возникновении события вызывается new Acceptor();
        SelectionKey selectionKey =
            _serverSocketChannel.register(_selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor(col));

    }

    public static void main(String[] args) {

        try /*(InputStream input = new FileInputStream("classes/connections.properties")) */ {
            //            Properties p = new Properties();
            //            p.load(input);
            _workerPool = Executors.newFixedThreadPool(WORKER_POOL_SIZE);

            MultipleSocketServer server =
                new MultipleSocketServer(6789/*Integer.parseInt(p.getProperty("port"))*/);
            Thread t = new Thread(server);
            t.setName("Multiserver thread");
            t.start();
            //            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // Acceptor: if connection is established, assign a handler to it.
    private class Acceptor implements Runnable {
        CheckCollissions col;

        public Acceptor(CheckCollissions col) {
            this.col = col;
        }

        public void run() {
            try {
                SocketChannel socketChannel = _serverSocketChannel.accept();
                if (socketChannel != null) {
                    new Handler(col, _selector, socketChannel);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            // Event Loop
            while (true) {
                _selector.select();
                Iterator it = _selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    SelectionKey sk = (SelectionKey) it.next();
                    if (sk.isValid()) {
                        it.remove();
                        Runnable r =
                            (Runnable) sk.attachment(); // handler or acceptor callback/runnable
                        if (r != null) {
                            r.run();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ExecutorService getWorkerPool() {
        return _workerPool;
    }

    public List<Client> getClientsList() {
        return clientsList;
    }



    public void startServer() {


        try (InputStream input = new FileInputStream("classes/connections.properties")) {
            Properties p = new Properties();
            ServerSocket socket1 = new ServerSocket(Integer.parseInt(p.getProperty("port")));
            ExecutorService executor = Executors.newCachedThreadPool();
            while (true) {

            }
        } catch (IOException e) {
            log.debug(e);
        }

        // Поток регистрации клиентов
        ServerRegistrationThread regThread = new ServerRegistrationThread(this);
        Thread thread = new Thread(regThread);
        thread.start();

        //Основной поток обработки данных
        while (true) {
            while (getClientsList().size() > 0 || clientList4Register.size() > 0) {
                getClientsList()
                    .addAll(clientList4Register);//добавить только что зарегистрированных клиентов
                clientList4Register.clear();
                getClientsList().removeAll(clientList4Delete);
                clientList4Delete.clear();

                for (Client client : getClientsList()) {
                    try {
                        //отправить запрос на данные
                        //                        askForData(client);
                        //                        getClientData(client);


                    } catch (Exception e) {
                        e.printStackTrace();
                        closeClient(client);
                        continue;

                    }
                }

                //Проверить столкновения

                //Сбор новых данных
                Clients sendClients = new Clients();
                for (Client client : getClientsList()) {
                    sendClients.getUuids().add(client.getUUID());
                    sendClients.getClientBalls().add(client.getBall());
                }
                for (Ball food : foods) {
                    sendClients.getCoordinateFoods().add(food);
                }
                //Отправка обновленных данных
                for (Client client : getClientsList()) {
                    try {
                        //                        sendNewData(client, sendClients);
                    } catch (Exception e) {
                        e.printStackTrace();
                        closeClient(client);
                        continue;
                    }
                }

                //Удаление съеденных клиентов
                for (Client client : getClientsList()) {
                    if (!client.getBall().isVisible()) {
                        closeClient(client);
                    }
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (lock) {
                try {

                    lock.wait();

                } catch (InterruptedException ie) {
                }
            }
        }
    }

    //    public void askForData(Client client) throws IOException {
    //        log.debug("Asking data from " + client);
    //        ObjectOutputStream out = client.getOos();
    //        out.writeObject(Client.DATA_REQUEST);
    //        out.flush();
    //        out.reset();
    //
    //
    //    }

    //    public void getClientData(Client client) throws IOException {
    //        log.debug("Getting data ");
    //        ObjectInputStream ois = client.getOis();
    //        try {
    //            String clientCursorPosition = (String) ois.readObject();
    //            client.setCursorPosition(parsePosition(clientCursorPosition));
    //            log.debug(client.getCursorPosition() + " from " + client);
    //            client.getBall().setCenterPosition(
    //                client.getBall().countNewFieldPosition(client.getCursorPosition()));
    //            log.debug("new Position " + client);
    //        } catch (ClassNotFoundException e) {
    //            e.printStackTrace();
    //            log.error(e);
    //        }
    //    }



    //    public void sendNewData(Client client, Clients clients) throws IOException {
    //        log.debug("sending Data " + clients.getClientBalls() + " ...");
    //        ObjectOutputStream out = client.getOos();
    //        //Отправляем данные всех клиентов этому клиенту
    //        out.writeObject(clients);
    //        out.flush();
    //        out.reset();
    //        log.debug("ok");
    //    }



    public void addClient(Client c) {
        clientList4Register.add(c);
    }



    public Point parsePosition(String input) {
        String[] temp = input.substring(input.indexOf("/") + 1).split("_");
        return new Point(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
    }

    public void closeClient(Client c) {
        clientList4Delete.add(c);
        try {
            c.getSocket().close();
        } catch (Exception e) {
        }
    }
}
