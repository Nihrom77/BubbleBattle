package net.mephi.swtproject.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import java.util.concurrent.ThreadLocalRandom;

public class Board extends Canvas {

    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;
    private final int DELAY = 100;

    private final int maxFoodAmount = 10;
    private Food[] food = new Food[maxFoodAmount];

    private int foodSize = 10;
    private int currentFoodAmount = maxFoodAmount;

    private int x;
    private int y;
    private int speed = 15;
    private int size = 25;

    private double speed1 = 0.15;

    private int dots;
    private int apple_x;
    private int apple_y;

    private boolean left = false;
    private boolean right = true;
    private boolean up = false;
    private boolean down = false;
    private boolean inGame = true;


    private Display display;
    private Shell shell;
    private Runnable runnable;

    public Board(Shell shell) {
        super(shell, SWT.NULL);

        this.shell = shell;

        initBoard();
    }

    private void initBoard() {

        display = shell.getDisplay();

        addListener(SWT.Paint, event -> doPainting(event));


        Color col = new Color(shell.getDisplay(), 0, 0, 0);

        setBackground(col);
        col.dispose();


        initGame();
    }


    private void initGame() {


        x = 50;
        y = 50;

        locateFood();

        runnable = new Runnable() {
            @Override
            public void run() {

                if (inGame) {
                    checkCollision();
                    checkFood();


                }

                display.timerExec(DELAY, this);
                redraw();
            }
        };

        display.timerExec(DELAY, runnable);
    }

    ;

    private void doPainting(Event e) {

        GC gc = e.gc;

        Color col = new Color(shell.getDisplay(), 0, 0, 0);
        gc.setBackground(col);
        col.dispose();

        gc.setAntialias(SWT.ON);

        if (inGame) {
            drawObjects(e);
        } else {
            gameOver(e);
        }
    }

    private void drawObjects(Event e) {

        GC gc = e.gc;
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        Point relativeCursorLocation = Display.getCurrent().getFocusControl().toControl(cursorLocation);
        double L2 = Math.sqrt(Math.pow(relativeCursorLocation.x - x, 2) + Math.pow(relativeCursorLocation.y - y, 2)) - speed;
        double L1 = speed;
        x = (int) ((relativeCursorLocation.x + ((L2 / L1) * x)) / (1 + L2 / L1));
        y = (int) ((relativeCursorLocation.y + ((L2 / L1) * y)) / (1 + L2 / L1));
        e.gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
        e.gc.fillOval(x, y, size, size);

        for (Food f : food) {
            if (f.isVisible()) {
                e.gc.setBackground(display.getSystemColor(f.getColor()));
                e.gc.fillOval(f.getX(), f.getY(), foodSize, foodSize);
            }
        }


    }

    private void gameOver(Event e) {

        GC gc = e.gc;

        String msg = "Game Over";

        Font font = new Font(e.display, "Helvetica", 12, SWT.NORMAL);
        Color whiteCol = new Color(e.display, 255, 255, 255);

        gc.setForeground(whiteCol);
        gc.setFont(font);

        Point size = gc.textExtent(msg);

        gc.drawText(msg, (WIDTH - size.x) / 2, (HEIGHT - size.y) / 2);

        font.dispose();
        whiteCol.dispose();

        display.timerExec(-1, runnable);
    }


    public void checkFood() {
//        if (currentFoodAmount < maxFoodAmount) {
//            for (Food f : food) {
//                if (!f.isVisible()) {
//                    f.setX(ThreadLocalRandom.current().nextInt(0, WIDTH + 1));
//                    f.setY(ThreadLocalRandom.current().nextInt(0, HEIGHT + 1));
//                    f.setColor(ThreadLocalRandom.current().nextInt(0, 16 + 1));
//                    f.setVisible(true);
//                }
//            }
//        }
    }

    public void checkCollision() {
        double minLen = Math.sqrt(foodSize * foodSize + size * size);
        for (Food f : food) {
            if (f.isVisible()) {

                double curLen = Math.sqrt(Math.pow(f.getX() - x, 2) + Math.pow(f.getY() - y, 2));

                if (curLen < minLen) {
                    f.setVisible(false);
                    currentFoodAmount--;
                    if(currentFoodAmount==0){
                        inGame=false;
                    }
                    descreaseSpeed();
                    increaseMass();
                    System.out.println("eat!!!");
                }
            }
        }


    }
public void descreaseSpeed(){
    if(speed>1){
        speed --;
    }
}
    public void increaseMass(){
        size+=5;
    }
    public void locateFood() {

        for (int i = 0; i < maxFoodAmount; i++) {
            Food f = new Food();
f.setColor(ThreadLocalRandom.current().nextInt(0, 16 + 1));
            f.setX(ThreadLocalRandom.current().nextInt(0, WIDTH + 1));
            f.setY(ThreadLocalRandom.current().nextInt(0, HEIGHT + 1));
            f.setVisible(true);
            food[i] = f;
        }
    }


}
