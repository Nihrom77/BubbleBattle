package net.mephi.client.components;

import net.mephi.server.Client;
import org.apache.commons.lang3.StringUtils;
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

public class Board extends Canvas {

    public static final int WIDTH = 10000;
    public static final int HEIGHT = 10000;


    public static final int FOOD_SIZE_RADIUS = (int) (WIDTH * 0.001 + 5);
    public static final int MAX_FOOD_AMOUNT = (int) (WIDTH * HEIGHT / FOOD_SIZE_RADIUS / 8000);
    public static final int BLACK_HOLES_AMOUNT = 5;

    private Logger log = Logger.getLogger(Board.class);


    private boolean inGame = true;
    private Point cursorLocation = new Point(Ball.WIDTH / 2, Ball.HEIGHT / 2);
    private Display display;
    private Shell shell;
    private String uuid;
    private JSONObject clients = new JSONObject();
    private JSONArray clientsArray = new JSONArray();
    private JSONArray foodArray = new JSONArray();
    private JSONArray blackHoleArray = new JSONArray();
    private JSONArray clientsTop5 = new JSONArray();
    private Point linesShift = new Point(0, 0);
    private Client client = null;
    private long fps = 1;
    private ImageFactory imageFactory = null;

    private final Object lock = new Object();

    public Board(Shell shell) {
        super(shell, SWT.DOUBLE_BUFFERED);

        this.shell = shell;
        display = shell.getDisplay();
        imageFactory = ImageFactory.getInstance(display);
        addListener(SWT.Paint, event -> doPainting(event));//Слушатель события redraw()
        Color col = new Color(shell.getDisplay(), 255, 255, 255);

        setBackground(col);
        col.dispose();

    }

    private void updateCursorLocation() {
        Point cursorMonitorLocation = Display.getCurrent().getCursorLocation();
        if (Display.getCurrent() != null && Display.getCurrent().getFocusControl() != null) {
            this.cursorLocation =
                Display.getCurrent().getFocusControl().toControl(cursorMonitorLocation);

        }
    }
    public Point getCursorLocation() {
        return this.cursorLocation;
    }

    public void setCursorLocation(Point p) {
        this.cursorLocation = p;
    }

    /**
     * Перерисовать доску.
     * Выполнется в потоке SWT.
     */
    public void refreshBoard(JSONObject clients, Point linesShift, Client client, long fps) {
        this.clients = clients;
        this.client = client;
        this.fps = fps;
        this.linesShift = linesShift;
        this.uuid = client.getUUID();
        clientsArray = (JSONArray) clients.get("clients");
        foodArray = (JSONArray) clients.get("food");
        blackHoleArray = (JSONArray) clients.get("blackhole");
        this.clientsTop5 = (JSONArray) clients.get("top5");
        if (clientsArray.size() > 0) {
            Display.getDefault().asyncExec(() -> {
                if (!shell.isDisposed()) {
                    redraw();
                }
            });
        }


    }

    private void doPainting(Event e) {
        updateCursorLocation();
        log.debug("doPainting");


        GC gc = e.gc;
        gc.setAntialias(SWT.ON);
        Color col = new Color(shell.getDisplay(), 255, 255, 255);
        gc.setBackground(col);
        col.dispose();

        //сетка на фоне
        Color colLine = new Color(shell.getDisplay(), 162, 162, 162);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setLineWidth(1);
        e.gc.setForeground(colLine);
        for (int i = 0; i < Ball.WIDTH; i += Ball.LINE_SPACE_SIZE) {

            gc.drawLine(0, i + linesShift.y, Ball.WIDTH, i + linesShift.y);//горизонтальные
            gc.drawLine(i + linesShift.x, 0, i + linesShift.x, Ball.HEIGHT);//вертикальные
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

            //нарисовать координаты курсора
            Font font = new Font(e.display, "Helvetica", 10, SWT.NORMAL);
            Color col1 = new Color(e.display, 0, 0, 0);
            gc.setFont(font);
            gc.setForeground(col1);
            gc.drawText(getCursorLocation().toString(), 20, 20);
            gc.drawText("FPS: " + String.valueOf(Math.round(1000.0 / (fps == 0 ? 1 : fps))), 20,
                50);
            font.dispose();
            col1.dispose();

            if (inGame) {
                drawObjects(e);
            } else {
                client.endGame();
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
                Color balCol = getSWTColorFromJSON((JSONObject) ball.get("color"), display);
                Color boardCol = get25PercentBrighterColor(display, balCol);
                e.gc.setBackground(boardCol);
                Point leftTop = Ball.getLeftTopPosition(center, radius);
                e.gc.fillOval(leftTop.x, leftTop.y, radius * 2, radius * 2);
                e.gc.setBackground(balCol);
                e.gc.fillOval(leftTop.x, leftTop.y, radius * 2 - (int) (radius * 2 * 0.02),
                    radius * 2 - (int) (radius * 2 * 0.02));

                //нарисовать имя
                Font font = new Font(e.display, "Helvetica", radius / 3, SWT.NORMAL);
                Color col = new Color(e.display, 0, 0, 0);
                gc.setFont(font);
                gc.setForeground(col);
                Point size = gc.textExtent((String) ball.get("name"));
                gc.drawText((String) ball.get("name"), center.x - size.x / 2,
                    center.y - size.y / 2);
                font.dispose();
                col.dispose();

            }


            for (int i = 0; i < foodArray.size(); i++) {
                JSONObject food = (JSONObject) foodArray.get(i);
                e.gc.setBackground(getSWTColorFromJSON((JSONObject) food.get("color"), display));
                Point center =
                    new Point(((Long) food.get("x")).intValue(), ((Long) food.get("y")).intValue());
                Point leftTop = Ball.getLeftTopPosition(center, Ball.FOOD_RADIUS);
                e.gc.fillOval(leftTop.x, leftTop.y, Ball.FOOD_RADIUS * 2, Ball.FOOD_RADIUS * 2);
            }

            for (int i = 0; i < blackHoleArray.size(); i++) {
                JSONObject hole = (JSONObject) blackHoleArray.get(i);
                Point leftTop =
                    new Point(((Long) hole.get("x")).intValue(), ((Long) hole.get("y")).intValue());
                int imageNum = ((Long) hole.get("id")).intValue();
                if (imageNum == 1) {
                    gc.drawImage(imageFactory.getBlackHole1(), leftTop.x, leftTop.y);
                } else if (imageNum == 2) {
                    gc.drawImage(imageFactory.getBlackHole2(), leftTop.x, leftTop.y);
                } else if (imageNum == 3) {
                    gc.drawImage(imageFactory.getBlackHole3(), leftTop.x, leftTop.y);
                } else if (imageNum == 4) {
                    gc.drawImage(imageFactory.getBlackHole4(), leftTop.x, leftTop.y);
                } else if (imageNum == 5) {
                    gc.drawImage(imageFactory.getBlackHole5(), leftTop.x, leftTop.y);
                }
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

        Font font = new Font(e.display, "Helvetica", 13, SWT.BOLD);
        Color col1 = new Color(e.display, 0, 0, 0);
        gc.setFont(font);
        gc.setForeground(col1);
        gc.drawText("TOP 5:", Ball.WIDTH - 130, 20);


        for (int i = 0; i < clientsTop5.size(); i++) {
            JSONObject ball = (JSONObject) clientsTop5.get(i);
            String name = (String) ball.get("name");
            int score = (((Long) ball.get("score")).intValue() - Ball.START_CLIENT_RADIUS);
            if (score < 0) {
                score = 0;
            }

            String out = i + 1 + ". " + StringUtils.rightPad(name, 5, '.') + "..." + StringUtils
                .leftPad(score + "", 3, '.');
            Point size = gc.textExtent(out);
            gc.drawText(out, Ball.WIDTH - 50 - size.x, 50 + (size.y * i));
        }
        font.dispose();
        col1.dispose();
    }

    public org.eclipse.swt.graphics.Color getSWTColor(java.awt.Color color, Display d) {
        return new Color(d, color.getRed(), color.getGreen(), color.getBlue());
    }

    public Color getSWTColorFromJSON(JSONObject color, Display d) {
        java.awt.Color c = new java.awt.Color(((Long) color.get("red")).intValue(),
            ((Long) color.get("green")).intValue(), ((Long) color.get("blue")).intValue());
        return getSWTColor(c, d);
    }

    public Color get25PercentBrighterColor(Display d, Color c) {
        //        int red = c.getRed();
        //        float fraction = 0.25f; // brighten by 25%
        //
        //        red = (int) (red + (red * fraction));
        return new Color(d, 0, 0, 0);
    }
}
