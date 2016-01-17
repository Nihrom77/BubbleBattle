package net.mephi.swtproject.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

import java.util.concurrent.ThreadLocalRandom;

public class Board extends Canvas {

    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final int DELAY = 100;

    private final int maxFoodAmount = 10;
    private Food[] food = new Food[maxFoodAmount];

    private int foodSize = 10;
    private int currentFoodAmount = maxFoodAmount;

    Ball ball = new Ball();//твой вправляемый шарик
    Ball[] enemies = new Ball[1];

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


        Color col = new Color(shell.getDisplay(), 255, 255, 255);

        setBackground(col);
        col.dispose();



        initGame();
    }


    private void initGame() {



        locateFood();
        locateEnemies();

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

        Color col = new Color(shell.getDisplay(), 255, 255, 255);
        gc.setBackground(col);
        col.dispose();

        //сетка на фоне
        Color colLine = new Color(shell.getDisplay(),162,162,162);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setLineWidth(1);
        e.gc.setForeground(colLine);
        for(int i = 0;i<WIDTH;i+=20){
            gc.drawLine( 0,  i, WIDTH, i);
            gc.drawLine( i,  0, i, HEIGHT);
        }
        colLine.dispose();


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
        if(cursorLocation!=null) {
            Control c = Display.getCurrent().getFocusControl();
            if (c != null) {

                //подвинуть шарик к курсору
                Point relativeCursorLocation = c.toControl(cursorLocation);
                ball.moveToCursor(relativeCursorLocation);
                e.gc.setBackground(display.getSystemColor(ball.getColor()));
                e.gc.fillOval(ball.leftTop.x, ball.leftTop.y, ball.radius*2, ball.radius*2);

                //нарисовать имя
                Font font = new Font(e.display, "Helvetica", ball.radius/3, SWT.NORMAL);
                Color col = new Color(e.display, 0, 0, 0);
                gc.setFont(font);
                gc.setForeground(col);
                Point size = gc.textExtent(ball.getName());
                gc.drawText(ball.getName(), ball.getCenterPosition().x-size.x/2, ball.getCenterPosition().y-size.y/2);
                font.dispose();
                col.dispose();

                for (Food f : food) {
                    if (f.isVisible()) {
                        e.gc.setBackground(display.getSystemColor(f.getColor()));
                        e.gc.fillOval(f.getLeftTop().x, f.getLeftTop().y, Food.FOOD_SIZE_RADIUS*2, Food.FOOD_SIZE_RADIUS*2);
                    }
                }
for(Ball enemy: enemies){
    if(enemy.isVisible()) {
        enemy.moveToCursor(new Point(ThreadLocalRandom.current().nextInt(0, WIDTH + 1), ThreadLocalRandom.current().nextInt(0, HEIGHT + 1)));
        e.gc.setBackground(display.getSystemColor(enemy.getColor()));
        e.gc.fillOval(enemy.leftTop.x, enemy.leftTop.y, enemy.radius * 2, enemy.radius * 2);
    }
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

        for (Food f : food) {
            if (f.isVisible()) {

                if (ball.checkCollisionTo(f)) {
                    f.setVisible(false);
                    currentFoodAmount--;
                    if(currentFoodAmount==0){
                        inGame=false;
                    }
                    ball.descreaseSpeed();
                    ball.increaseMass();
                    System.out.println("eat!!!");
                }

                for(Ball enemy:enemies){
                    if (enemy.isVisible() && enemy.checkCollisionTo(f)) {
                        f.setVisible(false);
                        currentFoodAmount--;
                        if(currentFoodAmount==0){
                            inGame=false;
                        }
                        enemy.descreaseSpeed();
                        enemy.increaseMass();
                        System.out.println("enemy eat!!!");
                    }

                    if(enemy.isVisible() && ball.checkCollisionTo(enemy)){
                        if( enemy.getRadius()>ball.getRadius()){
                            ball.setRadius(0);

                            inGame=false;
                        }else{
                            ball.descreaseSpeed();
                            ball.increaseMass();
                            enemy.setRadius(0);
                            enemy.setVisible(false);
                        }
                    }
                }

            }
        }


    }

    public void locateFood() {

        for (int i = 0; i < maxFoodAmount; i++) {
            Food f = new Food();
            f.setColor(ThreadLocalRandom.current().nextInt(0, 16 + 1));
            f.setPosition(new Point(ThreadLocalRandom.current().nextInt(0, WIDTH + 1),ThreadLocalRandom.current().nextInt(0, HEIGHT + 1)));
            f.setVisible(true);
            food[i] = f;
        }
    }

    public void locateEnemies(){
        for(int i = 0; i< enemies.length; i++){
            Ball enemy = new Ball();
            enemy.setName("враг");
            enemy.moveTo(new Point(ThreadLocalRandom.current().nextInt(0, WIDTH + 1),ThreadLocalRandom.current().nextInt(0, HEIGHT + 1)));
            enemies[i] = enemy;
        }
    }

public void setBallName(String name){
    ball.setName(name);
}
}
