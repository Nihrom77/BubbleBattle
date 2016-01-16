package net.mephi.swtproject.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import java.io.InputStream;

public class Board extends Canvas {

    private final int WIDTH = 300;
    private final int HEIGHT = 300;
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;
    private final int DELAY = 100;


    private int x;
    private int y;
private int speed = 15;
    private int size = 25;

    private int dots;
    private int apple_x;
    private int apple_y;

    private boolean left = false;
    private boolean right = true;
    private boolean up = false;
    private boolean down = false;
    private boolean inGame = true;

    private Image ball;
    private Image apple;
    private Image head;

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
        addListener(SWT.KeyDown, event -> onKeyDown(event));

        addListener(SWT.Dispose, event -> {

            ball.dispose();
            apple.dispose();
            head.dispose();
        });

        Color col = new Color(shell.getDisplay(), 0, 0, 0);

        setBackground(col);
        col.dispose();

        loadImages();

        initGame();
    }

    private void loadImages() {

    }

    private void initGame() {



        x = 50;
        y = 50;

//        locateApple();

        runnable = new Runnable() {
            @Override
            public void run() {

                if (inGame) {
//                    checkApple();
//                    checkCollision();
                    move();

                }

                display.timerExec(DELAY, this);
                redraw();
            }
        };

        display.timerExec(DELAY, runnable);
    };

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
//        System.out.println("mouse: "+relativeCursorLocation.x+ ":"+relativeCursorLocation.y);
        int addX = relativeCursorLocation.x - x;
        int addY = relativeCursorLocation.y - y;
//        System.out.println("add: "+addX+ ":"+addY);
//        System.out.println("before: "+x+ ":"+y);
        double L2 = Math.sqrt(Math.pow(relativeCursorLocation.x - x,2)+Math.pow(relativeCursorLocation.y - y,2))-speed;
        double L1 = speed;
        x = (int)((relativeCursorLocation.x + ((L2/L1)*x))/(1 + L2/L1));
        y = (int)((relativeCursorLocation.y+ ((L2/L1)*y))/(1 + L2/L1));
//        x = addX < 0 ? x  - speed :x  + speed  ;
//        y = addY < 0 ? y  - speed :y  + speed  ;
//        System.out.println("after: "+x+ ":"+y);
//                gc.drawImage(head, x, y);
        e.gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
        e.gc.fillOval(x,y,size,size);
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



    private void move() {



    }

    public void checkCollision() {



        if (y > HEIGHT ) {
            inGame = false;
        }

        if (y < 0) {
            inGame = false;
        }

        if (x > WIDTH) {
            inGame = false;
        }

        if (x< 0) {
            inGame = false;
        }
    }

    public void locateApple() {

        int r = (int) (Math.random() * RAND_POS);
        apple_x = ((r * DOT_SIZE));
        r = (int) (Math.random() * RAND_POS);
        apple_y = ((r * DOT_SIZE));
    }

    private void onKeyDown(Event e) {

        int key = e.keyCode;

        if ((key == SWT.ARROW_LEFT) && (!right)) {
            left = true;
            up = false;
            down = false;
        }

        if ((key == SWT.ARROW_RIGHT) && (!left)) {
            right = true;
            up = false;
            down = false;
        }

        if ((key == SWT.ARROW_UP) && (!down)) {
            up = true;
            right = false;
            left = false;
        }

        if ((key == SWT.ARROW_DOWN) && (!up)) {
            down = true;
            right = false;
            left = false;
        }
    }
}
