package net.mephi.client.components;

import net.mephi.client.Clients;
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

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;

public class Board extends Canvas {

    public static final int WIDTH = (int)(Toolkit.getDefaultToolkit().getScreenSize().width*0.9);
    public static final int HEIGHT =  (int)(Toolkit.getDefaultToolkit().getScreenSize().height*0.9);
    public static final int FOOD_SIZE_RADIUS = 10;
    public static final int MAX_FOOD_AMOUNT = WIDTH * HEIGHT / FOOD_SIZE_RADIUS / 8000;
    private Logger log = Logger.getLogger(Board.class);

    private Clients clients = new Clients();

    private boolean inGame = true;
    private Point cursorLocation = new Point(WIDTH/2,HEIGHT/2);
    private Display display;
    private Shell shell;
    private String uuid;

    public Board(Shell shell) {
        super(shell, SWT.DOUBLE_BUFFERED);

        this.shell = shell;
        display = shell.getDisplay();
        addListener(SWT.Paint, event -> doPainting(event));//Слушатель события redraw()
        Color col = new Color(shell.getDisplay(), 255, 255, 255);

        setBackground(col);
        col.dispose();

    }

    public  Point getCursorLocation() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {

                Point cursorLocation = Display.getCurrent().getCursorLocation();
                Point relativeCursorLocation = Display.getCurrent().getFocusControl().toControl(cursorLocation);

                setCursorLocation(relativeCursorLocation);
            }
        });
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.cursorLocation;
    }

public void setCursorLocation(Point p){
    this.cursorLocation = p;
}
    public void refreshBoard(Clients clients, String uuid) {
        this.clients = clients;
        this.uuid = uuid;
if(clients.getUuids().size()>0) {
    Display.getDefault().asyncExec(new Runnable() {
        public void run() {
            redraw();
        }
    });
}


    }


    private void doPainting(Event e) {
        GC gc = e.gc;
        Color col = new Color(shell.getDisplay(), 255, 255, 255);
        gc.setBackground(col);
        col.dispose();

        //сетка на фоне
        Color colLine = new Color(shell.getDisplay(), 162, 162, 162);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setLineWidth(1);
        e.gc.setForeground(colLine);
        for (int i = 0; i < WIDTH; i += 20) {
            gc.drawLine(0, i, WIDTH, i);
            gc.drawLine(i, 0, i, HEIGHT);
        }
        colLine.dispose();
        log.debug("Number of clients = "+clients.getUuids().size());
        if(clients.getUuids().size()>0) {
            drawTopScores(e,clients);

            for (int i = 0; i < clients.getUuids().size(); ++i) {
                if (clients.getUuids().get(i).equals(uuid) && clients.getClientBalls().get(i).getRadius() == Ball.END_GAME_RADIUS) {
                    inGame = false;
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
            for (int i = 0; i < clients.getUuids().size(); ++i) {
                e.gc.setBackground(getSWTColor(clients.getClientBalls().get(i).getColor(), display));
                e.gc.fillOval(clients.getClientBalls().get(i).getLeftTopPosition().x, clients.getClientBalls().get(i).getLeftTopPosition().y, clients.getClientBalls().get(i).getRadius() * 2, clients.getClientBalls().get(i).getRadius() * 2);

                //нарисовать имя
                Font font = new Font(e.display, "Helvetica", clients.getClientBalls().get(i).getRadius() / 3, SWT.NORMAL);
//                Font font1 = new Font(e.display, "Helvetica", 10, SWT.NORMAL);
                Color col = new Color(e.display, 0, 0, 0);
                gc.setFont(font);
                gc.setForeground(col);
                Point size = gc.textExtent(clients.getClientBalls().get(i).getName());
                gc.drawText(clients.getClientBalls().get(i).getName(), clients.getClientBalls().get(i).getCenterPosition().x - size.x / 2, clients.getClientBalls().get(i).getCenterPosition().y - size.y / 2);
//                gc.setFont(font1);
//                gc.drawText(clients.getClientBalls().get(i).getCenterPosition()+"   "+clients.getClientBalls().get(i).getRadius()+"   "+clients.getClientBalls().get(i).getSpeed(),20,50);
                font.dispose();
//                font1.dispose();
                col.dispose();
            }


            for (Ball f : clients.getCoordinateFoods()) {
                if (f.isVisible()) {
                    e.gc.setBackground(getSWTColor(f.getColor(), display));
                    e.gc.fillOval(f.getLeftTopPosition().x, f.getLeftTopPosition().y, Ball.FOOD_RADIUS * 2, Ball.FOOD_RADIUS * 2);
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

        gc.drawText(msg, (WIDTH - size.x) / 2, (HEIGHT - size.y) / 2);

        font.dispose();
        whiteCol.dispose();

    }



private void drawTopScores(Event e,Clients clients){
    GC gc = e.gc;

    Font font = new Font(e.display, "Helvetica", 10, SWT.BOLD);
    Color col1 = new Color(e.display, 0, 0, 0);
    gc.setFont(font);
    gc.setForeground(col1);
    gc.drawText("TOP 5:", Board.WIDTH-100, 20);


Collections.sort(clients.getClientBalls(), new Comparator<Ball>() {
    @Override
    public int compare(Ball o1, Ball o2) {
        return (int)Math.signum(o2.getRadius()-o1.getRadius()) ;
    }
});
    for(int i=0;i< Math.min(5,clients.getClientBalls().size());i++) {
        String name = (clients.getClientBalls().get(i).getName() + "....." + (clients.getClientBalls().get(i).getRadius() - Ball.START_CLIENT_RADIUS));
        Point size = gc.textExtent(name);
        gc.drawText(i+1+". "+name, Board.WIDTH-50-size.x, 40+(size.y*i));
    }
    font.dispose();
    col1.dispose();
}

    public org.eclipse.swt.graphics.Color getSWTColor(java.awt.Color color, Display d) {
        return new org.eclipse.swt.graphics.Color(d, color.getRed(), color.getGreen(), color.getBlue());
    }
}
