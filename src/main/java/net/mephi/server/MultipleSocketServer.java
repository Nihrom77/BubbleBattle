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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MultipleSocketServer {

    private List<Client> clientsList = new ArrayList<>();
    private List<Client> clientList4Delete = new ArrayList<>();
    private Logger log = Logger.getLogger(MultipleSocketServer.class);


    private List<Client> clientList4Register = new ArrayList<>();
    private Ball[] foods = new Ball[Board.MAX_FOOD_AMOUNT];
    public Object lock = new Object();

    public static void main(String[] args) {

        MultipleSocketServer server = new MultipleSocketServer();

            server.startServer();



    }

    public  List<Client> getClientsList() {
        return clientsList;
    }

    public void locateFood() {
        log.debug("MAX_FOOD_AMOUNT="+Board.MAX_FOOD_AMOUNT);
        for (int i = 0; i < Board.MAX_FOOD_AMOUNT; i++) {
            Ball f = new Ball(Ball.FOOD_RADIUS);
            Random r = new Random();
            java.awt.Color color = java.awt.Color.getHSBColor(r.nextFloat(), r.nextFloat(), r.nextFloat());
            f.setColor(color);
            f.setRandomPosition();
            f.setVisible(true);
            foods[i] = f;
        }
    }

    public void startServer() {

        //Инициализация массива еды
        locateFood();


        // Поток регистрации клиентов
        ServerRegistrationThread regThread = new ServerRegistrationThread(this);
        Thread thread = new Thread(regThread);
        thread.start();

        //Основной поток обработки данных
        while(true) {
            while (getClientsList().size() > 0 || clientList4Register.size()>0) {
                getClientsList().addAll(clientList4Register);//добавить только что зарегистрированных клиентов
                clientList4Register.clear();
                getClientsList().removeAll(clientList4Delete);
                clientList4Delete.clear();

                for (Client client : getClientsList()) {
                    try {
                        //отправить запрос на данные
                        askForData(client);
                        getClientData(client);


                    } catch (Exception e) {
                        e.printStackTrace();
                        closeClient(client);
                        continue;

                    }
                }

                //Проверить столкновения
                checkCollissions();

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
                        sendNewData(client, sendClients);
                    } catch (Exception e) {
                        e.printStackTrace();
                        closeClient(client);
                        continue;
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

    public void askForData(Client client) throws IOException {
        log.debug("Asking data from "+client);
        ObjectOutputStream out = client.getOos();
        out.writeObject(Client.DATA_REQUEST);
        out.flush();
        out.reset();


    }

    public void getClientData(Client client) throws IOException {
        log.debug("Getting data ");
        ObjectInputStream ois = client.getOis();
        try {
            String clientCursorPosition = (String) ois.readObject();
            client.setCursorPosition(parsePosition(clientCursorPosition));
            log.debug(client.getCursorPosition() +" from "+client);
            client.getBall().setCenterPosition(client.getBall().countNewCenterPosition(client.getCursorPosition()));
            log.debug("new Position "+client);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.error(e);
        }
    }

    public void checkCollissions() {

        //столкновения клиентов и еды
        for (Ball food : foods) {
            if (food.isVisible()) {
                for (Client client : clientsList) {
                    if (client.getBall().checkCollisionTo(food)) {
                        food.setVisible(false);

                        client.getBall().increaseMass(food);
                        log.debug("Client "+client+" eated");
                    }
                }


            }
        }
        //Съеденную еду перенести на новое место
        for(Ball food : foods){
            if(!food.isVisible()){
                food.setRandomPosition();
                food.setVisible(true);
            }
        }

        //пересечение клиентов друг с другом
        for (Client client1 : getClientsList()) {
            for (Client client2 : getClientsList()) {
                if (client1.getBall().checkCollisionTo(client2.getBall()) && !client1.getUUID().equals(client2.getUUID())) {
                    client2.getBall().setRadius(Ball.END_GAME_RADIUS);

                    client1.getBall().increaseMass(client2.getBall());
                    log.debug("Client "+client1+" ate Client "+client2);
                }
            }
        }


    }

    public void sendNewData(Client client, Clients clients) throws IOException {
        log.debug("sending Data "+clients.getClientBalls()+" ...");
        ObjectOutputStream out =client.getOos();
        //Отправляем данные всех клиентов этому клиенту
        out.writeObject(clients);
        out.flush();
        out.reset();
        log.debug("ok");
    }

    public static int getPort() {
        return 6789;
    }

    public  void addClient(Client c) {
        clientList4Register.add(c);
    }

    public Point parsePosition(String input) {
        String[] temp = input.substring(input.indexOf("/") + 1).split("_");
        return new Point(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
    }
    public void closeClient(Client c){
        clientList4Delete.add(c);
        try {
            c.getSocket().close();
        }catch (Exception e){}
    }
}
