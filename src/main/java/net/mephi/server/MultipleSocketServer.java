package net.mephi.server;

import net.mephi.client.components.Ball;
import net.mephi.client.components.Board;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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



    //    public void closeClient(Client c) {
    //        clientList4Delete.add(c);
    //        try {
    //            c.getSocket().close();
    //        } catch (Exception e) {
    //            log.error(e);
    //        }
    //    }
}
