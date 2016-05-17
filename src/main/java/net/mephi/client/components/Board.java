package net.mephi.client.components;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.Comparator;

public class Board extends Canvas {

    public static final int WIDTH = 3000;
    public static final int HEIGHT = 3000;


    public static final int FOOD_SIZE_RADIUS = (int) (WIDTH * 0.001 + 5);
    public static final int MAX_FOOD_AMOUNT = (int) (WIDTH * HEIGHT / FOOD_SIZE_RADIUS / 8000);
    private Logger log = Logger.getLogger(Board.class);


    private boolean inGame = true;
    private Point cursorLocation = new Point(Ball.WIDTH / 2, Ball.HEIGHT / 2);
    private Display display;
    private Shell shell;
    private String uuid;
    private JSONObject clients = new JSONObject();
    private JSONArray clientsArray = new JSONArray();
    private JSONArray foodArray = new JSONArray();
    private Point linesShift = new Point(0, 0);

    private final Object lock = new Object();

    public Board(Shell shell) {
        super(shell, SWT.DOUBLE_BUFFERED);

        this.shell = shell;
        display = shell.getDisplay();
        addListener(SWT.Paint, event -> doPainting(event));//Слушатель события redraw()

        Color col = new Color(shell.getDisplay(), 255, 255, 255);

        setBackground(col);
        col.dispose();

    }

    public Point getCursorLocation() {

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {

                Point cursorLocation = Display.getCurrent().getCursorLocation();
                Point relativeCursorLocation =
                    Display.getCurrent().getFocusControl().toControl(cursorLocation);

                setCursorLocation(relativeCursorLocation);
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            try {
                lock.wait(20);
                //                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        return this.cursorLocation;
    }

    public void setCursorLocation(Point p) {
        this.cursorLocation = p;
    }

    /**
     * Перерисовать доску.
     * Выполнется в потоке SWT.
     *
     * @param uuid
     */
    public void refreshBoard(JSONObject clients, String uuid, Point linesShift) {
        this.clients = clients;
        //TODO: работает так себе
        //        if (this.linesShift.x == linesShift.x || this.linesShift.y == linesShift.y) {
        //        } else {
            this.linesShift.x = (this.linesShift.x + linesShift.x) % Ball.LINE_SPACE_SIZE;
            this.linesShift.y = (this.linesShift.y + linesShift.y) % Ball.LINE_SPACE_SIZE;
        //        }
        this.uuid = uuid;
        clientsArray = (JSONArray) clients.get("clients");
        foodArray = (JSONArray) clients.get("food");
        if (clientsArray.size() > 0) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    redraw();
                }
            });
        }


    }

    private void doPainting(Event e) {
        log.debug("doPainting");
        GC gc = e.gc;
        Color col = new Color(shell.getDisplay(), 255, 255, 255);
        gc.setBackground(col);
        col.dispose();

        //сетка на фоне
        Color colLine = new Color(shell.getDisplay(), 162, 162, 162);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setLineWidth(1);
        e.gc.setForeground(colLine);
        for (int i = 0; i < Ball.WIDTH; i += Ball.LINE_SPACE_SIZE) {
            gc.drawLine(0, i + linesShift.y, Ball.WIDTH, i + linesShift.y);
            gc.drawLine(i + linesShift.x, 0, i + linesShift.x, Ball.HEIGHT);
        }
        colLine.dispose();
        log.debug("Number of clients = " + clientsArray.size());
        if (clientsArray.size() > 0) {
            drawTopScores(e);

            for (int i = 0; i < clientsArray.size(); ++i) {
                JSONObject ball = (JSONObject) clientsArray.get(i);
                if (ball.get("uuid").equals(uuid)
                    && ((Long) ball.get("radius")).intValue() == Ball.END_GAME_RADIUS) {
                    inGame = false;
                    log.debug("inGame = false");
                    break;
                }
            }

            //нарисовать координаты
            Font font = new Font(e.display, "Helvetica", 10, SWT.NORMAL);
            Color col1 = new Color(e.display, 0, 0, 0);
            gc.setFont(font);
            gc.setForeground(col1);
            gc.drawText(Display.getCurrent().getCursorLocation().toString(), 20, 20);
            font.dispose();
            col1.dispose();

            if (inGame) {
                drawObjects(e);
            } else {
                gameOver(e);
            }
        }
    }

    private void drawObjects(Event e) {
        GC gc = e.gc;

        Control c = Display.getCurrent().getFocusControl();
        if (c != null) {
            for (int i = 0; i < clientsArray.size(); ++i) {
                JSONObject ball = (JSONObject) clientsArray.get(i);
                Point center =
                    new Point(((Long) ball.get("x")).intValue(), ((Long) ball.get("y")).intValue());
                int radius = ((Long) ball.get("radius")).intValue();

                e.gc.setBackground(getSWTColorFromJSON((JSONObject) ball.get("color"), display));
                Point leftTop = Ball.getLeftTopPosition(center, radius);
                e.gc.fillOval(leftTop.x, leftTop.y, radius * 2, radius * 2);

                //нарисовать имя
                Font font = new Font(e.display, "Helvetica", radius / 3, SWT.NORMAL);
                //                Font font1 = new Font(e.display, "Helvetica", 10, SWT.NORMAL);
                Color col = new Color(e.display, 0, 0, 0);
                gc.setFont(font);
                gc.setForeground(col);
                Point size = gc.textExtent((String) ball.get("name"));
                gc.drawText((String) ball.get("name"), center.x - size.x / 2,
                    center.y - size.y / 2);
                //                gc.setFont(font1);
                //                gc.drawText(clients.getClientBalls().get(i).getCenterLocalPosition()+"   "+clients.getClientBalls().get(i).getRadius()+"   "+clients.getClientBalls().get(i).getSpeed(),20,50);
                font.dispose();
                //                font1.dispose();
                col.dispose();
            }


            for (int i = 0; i < foodArray.size(); i++) {
                JSONObject food = (JSONObject) foodArray.get(i);
                //                if (f.isVisible()) {
                e.gc.setBackground(getSWTColorFromJSON((JSONObject) food.get("color"), display));
                Point center =
                    new Point(((Long) food.get("x")).intValue(), ((Long) food.get("y")).intValue());
                Point leftTop = Ball.getLeftTopPosition(center, Ball.FOOD_RADIUS);
                e.gc.fillOval(leftTop.x, leftTop.y, Ball.FOOD_RADIUS * 2, Ball.FOOD_RADIUS * 2);
                //                }
            }

        }
    }


    private void gameOver(Event e) {

        GC gc = e.gc;

        String msg = "Game Over";

        Font font = new Font(e.display, "Helvetica", 12, SWT.NORMAL);
        Color whiteCol = new Color(e.display, 20, 20, 20);

        gc.setForeground(whiteCol);
        gc.setFont(font);

        Point size = gc.textExtent(msg);

        gc.drawText(msg, (Ball.WIDTH - size.x) / 2, (Ball.HEIGHT - size.y) / 2);

        font.dispose();
        whiteCol.dispose();

    }



    private void drawTopScores(Event e) {
        GC gc = e.gc;

        Font font = new Font(e.display, "Helvetica", 10, SWT.BOLD);
        Color col1 = new Color(e.display, 0, 0, 0);
        gc.setFont(font);
        gc.setForeground(col1);
        gc.drawText("TOP 5:", Ball.WIDTH - 100, 20);


        Collections.sort(clientsArray, new Comparator<Object>() {
            @Override public int compare(Object o1, Object o2) {
                JSONObject obj1 = (JSONObject) o1;
                JSONObject obj2 = (JSONObject) o2;
                return (int) Math.signum(
                    ((Long) obj1.get("radius")).intValue() - ((Long) obj2.get("radius"))
                        .intValue());
            }
        });
        for (int i = 0; i < Math.min(5, clientsArray.size()); i++) {
            JSONObject ball = (JSONObject) clientsArray.get(i);
            String name = ball.get("name") + "....." + (((Long) ball.get("radius")).intValue()
                - Ball.START_CLIENT_RADIUS);
            Point size = gc.textExtent(name);
            gc.drawText(i + 1 + ". " + name, Ball.WIDTH - 50 - size.x, 40 + (size.y * i));
        }
        font.dispose();
        col1.dispose();
    }

    public org.eclipse.swt.graphics.Color getSWTColor(java.awt.Color color, Display d) {
        return new org.eclipse.swt.graphics.Color(d, color.getRed(), color.getGreen(),
            color.getBlue());
    }

    public org.eclipse.swt.graphics.Color getSWTColorFromJSON(JSONObject color, Display d) {
        java.awt.Color c = new java.awt.Color(((Long) color.get("red")).intValue(),
            ((Long) color.get("green")).intValue(), ((Long) color.get("blue")).intValue());
        return getSWTColor(c, d);
    }
}
