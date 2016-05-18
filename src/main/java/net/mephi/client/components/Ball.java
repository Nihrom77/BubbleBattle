package net.mephi.client.components;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.awt.*;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Acer on 16.01.2016.
 */
public class Ball implements Serializable {
    private Logger log = Logger.getLogger(Ball.class);

    public static final int WIDTH = (int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.9);
    public static final int HEIGHT =
        (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.9);
    public static final int START_CLIENT_RADIUS = (int) (HEIGHT * 0.05);
    public static final int FOOD_RADIUS = START_CLIENT_RADIUS / 4;
    public static final int END_GAME_RADIUS = 0;
    public static final int MAX_RADIUS = HEIGHT / 3;
    public static final String FOOD_NAME = "";
    public static final int LINE_SPACE_SIZE = START_CLIENT_RADIUS;
    private Point center = new Point(0, 0);
    private int radius;
    private Point cursorLocation = new Point(0, 0);

    private Rectangle userField = new Rectangle(0, 0, 0, 0);

    private String name = "";
    private boolean visible = true;
    private Color color;
    private boolean isFood = false;
    private Point linesShift = new Point(0, 0);

    public Ball(String name, int radius) {
        this.radius = radius;
        this.name = name;
        setRandomColor();
    }

    /**
     * Положение центра в локальных координатах
     *
     * @return
     */
    public Point getCenterLocalPosition() {
        return new Point(userField.width / 2, userField.height / 2);
    }

    public Point getCenterGlobalPosition() {
        if (isFood) {
            return center;
        } else {
            return new Point(userField.x + userField.width / 2, userField.y + userField.height / 2);
        }
    }

    public void setCenterPosition(Point newCenter) {
        this.center = newCenter;
    }

    public Point getCenter() {
        return center;
    }

    public static Point getLeftTopPosition(Point center, int radius) {
        return new Point(center.x - radius, center.y - radius);
    }

    public void setCursorLocation(Point p) {
        cursorLocation = p;
    }

    public Point getCursorLocation() {
        return cursorLocation;
    }

    public int getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public Rectangle getUserField() {
        return userField;
    }

    public void setUserField(Rectangle rect) {
        this.userField = rect;
    }

    public void setUserFieldPosition(Point p) {
        userField.x = p.x;
        userField.y = p.y;
        //        setCenterPosition(new Point(p.x / 2, p.y / 2));
    }

    public void setFood(boolean isFood) {
        this.isFood = isFood;
    }

    public boolean isFood() {
        return isFood;
    }

    public void increaseMass(Ball b) {
        int r = (int) (Math.sqrt(radius * radius + (b.getRadius() * b.getRadius())));
        if (r > MAX_RADIUS) {
            radius = MAX_RADIUS;
        } else {
            radius = r;
        }

    }

    public int getSpeed() {
        if (Ball.START_CLIENT_RADIUS - (radius - Ball.START_CLIENT_RADIUS) / 5 < 4) {
            return 4;
        } else {
            return Ball.START_CLIENT_RADIUS - (radius - Ball.START_CLIENT_RADIUS) / 8;
        }
    }


    /**
     * Рассчитать следующюю позицию поля(левый верхний угол) шарика по полученным координатам мыши.
     *
     * @param relativeCursorLocation локальные координаты курсора
     * @return
     */
    public Point countNewFieldPosition(Point relativeCursorLocation) {
        //Магия подобных треугольников по углу и подобным сторонам.
        //ABC ~ A1B1C1 A -общий -> AB/A1B1 = AC/A1C1 = BC/B1C1
        //
        //log.debug("cursor position "+relativeCursorLocation);
        double AB = Math.sqrt(
            Math.pow(relativeCursorLocation.x - getCenterLocalPosition().x, 2) + Math
                .pow(relativeCursorLocation.y - getCenterLocalPosition().y, 2));//всегда >0
        double AC = relativeCursorLocation.x - getCenterLocalPosition().x;//<0 если шаг влево
        double BC = relativeCursorLocation.y - getCenterLocalPosition().y;//<0 если шаг вверх
        double A1B1 = radius * 0.1;

        double A1C1 = A1B1 * AC / AB;//<0 если шаг влево
        double B1C1 = A1B1 * BC / AB;//<0 если шаг вверх



        int x = (int) Math.round(getCenterLocalPosition().x + A1C1);
        int y = (int) Math.round(getCenterLocalPosition().y + B1C1);



        //Если шаг перескакивает курсор
        if (relativeCursorLocation.x > getCenterLocalPosition().x) {
            if (x > relativeCursorLocation.x) {
                return new Point(userField.x, userField.y);
            }
        } else {
            if (x < relativeCursorLocation.x) {
                return new Point(userField.x, userField.y);
            }
        }

        if (relativeCursorLocation.y < getCenterLocalPosition().y) {
            if (y < relativeCursorLocation.y) {
                return new Point(userField.x, userField.y);
            }
        } else {
            if (y > relativeCursorLocation.y) {
                return new Point(userField.x, userField.y);
            }
        }



        int xGlobal = userField.x + (x - getCenterLocalPosition().x);//глобальные
        int yGlobal = userField.y + (y - getCenterLocalPosition().y);

        boolean freezX = false, freezY = false;
        //Проверка выхода за глобальные границы
        if (xGlobal + userField.width / 2 > Board.WIDTH) {//правый
            xGlobal = Board.WIDTH - userField.width / 2;
            freezX = true;
        }
        if (yGlobal + userField.height / 2 > Board.HEIGHT) {//низ
            yGlobal = Board.HEIGHT - userField.height / 2;
            freezY = true;
        }
        if (xGlobal + userField.width / 2 < 0) {//лево
            xGlobal = -userField.width / 2;
            freezX = true;
        }
        if (yGlobal + userField.height / 2 < 0) {//вверх
            yGlobal = -userField.height / 2;
            freezY = true;
        }

        updateLineShift(x, y, freezX, freezY);


        log.debug("lineshift new " + linesShift);
        userField.x = xGlobal;
        userField.y = yGlobal;
        Point p = new Point(x + userField.width / 2, y + userField.height / 2);
        return p;
    }

    /**
     * Проверка столкновения
     *
     * @param enemyBall
     * @return true, если enemyBall умер
     */
    public boolean checkCollisionTo(Ball enemyBall) {
        double minCollisionLength =
            (radius - radius * 0.05) + (enemyBall.getRadius() - enemyBall.getRadius() * 0.05);
        if (getCenterDistance(this.getCenterGlobalPosition(), enemyBall.getCenterGlobalPosition())
            < minCollisionLength) {
            return enemyBall.getRadius() < this.getRadius();
        }
        return false;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getName() {
        return name;
    }

    /**
     * Выставить случайную позицию на поле.
     * Считается на сервере
     * Отступ от границ - 5%
     */
    public void setRandomFieldPosition() {
        userField.x = ThreadLocalRandom.current()
            .nextInt(0, (int) (userField.width - userField.width * 0.05) + 1);
        userField.y = ThreadLocalRandom.current()
            .nextInt(0, (int) (userField.height - userField.height * 0.05) + 1);
        //        userField.width = WIDTH;
        //        userField.height = HEIGHT;
        //        center = new Point(userField.x +userField.width /2, userField.y + userField.height / 2); //локальные
    }

    public void setRandomCenterPosition() {
        int x =
            ThreadLocalRandom.current().nextInt(0, (int) (Board.WIDTH - Board.WIDTH * 0.05) + 1);
        int y =
            ThreadLocalRandom.current().nextInt(0, (int) (Board.HEIGHT - Board.HEIGHT * 0.05) + 1);
        center = new Point(x, y); //глобальные для еды.
    }

    public void setRandomColor() {
        Random r = new Random();
        color = java.awt.Color.getHSBColor(r.nextFloat(), r.nextFloat(), r.nextFloat());

    }

    /**
     * Расстояние между центрами
     *
     * @param center1
     * @param center2
     * @return
     */
    public double getCenterDistance(Point center1, Point center2) {
        return Math.sqrt(Math.pow(center2.x - center1.x, 2) + Math.pow(center2.y - center1.y, 2));
    }

    public Point getLeftTopFieldPosition() {
        return new Point(userField.x, userField.y);
    }

    public boolean isBallInCurrentField(Ball otherBall) {
        Point center;
        if (otherBall.isFood()) {
            center = otherBall.center;
        } else {
            center = otherBall.getCenterGlobalPosition();
        }
        return center.x >= userField.x && center.x <= userField.x + userField.width
            && center.y >= userField.y && center.y <= userField.y + userField.height;
    }

    public Point getLinesShift() {
        return linesShift;
    }

    @Override public String toString() {
        return getName() + " " + getRadius() + " " + getCenterLocalPosition();
    }

    /**
     * Поскольку смещается поле, а шарик всегда в центре, то двигаем сетку на фоне.
     * Пересчет смещения сетки.
     *
     * @param x      - новые координаты центра, после шага.
     * @param y      - новые координаты центра, после шага.
     * @param freezX - достигли края (право или лево)
     * @param freezY - достигла края (верх или них)
     * @return смещение сетки.
     */
    public Point updateLineShift(int x, int y, boolean freezX, boolean freezY) {
        log.debug("lineshift old " + linesShift);


        //Смещение сетки на доске 1.
        int tempW = (getCenterLocalPosition().x - x) % Ball.LINE_SPACE_SIZE;
        int tempH = (getCenterLocalPosition().y - y) % Ball.LINE_SPACE_SIZE;

        this.linesShift.x = (freezX ? linesShift.x : linesShift.x + tempW) % Ball.LINE_SPACE_SIZE;
        this.linesShift.y = (freezY ? linesShift.y : linesShift.y + tempH) % Ball.LINE_SPACE_SIZE;

        //Смещение сетки на доске 2.
        //        linesShift.x = (int)Math.round((linesShift.x - A1B1)%Ball.LINE_SPACE_SIZE);
        //        linesShift.y = (int)Math.round((linesShift.y - B1C1)%Ball.LINE_SPACE_SIZE);
        //        log.debug("lineshift "+linesShift);
        //        if(linesShift.x <0){
        //            linesShift.x = Ball.LINE_SPACE_SIZE - linesShift.x;
        //        }
        //        if(linesShift.y <0){
        //            linesShift.y = Ball.LINE_SPACE_SIZE + linesShift.y;
        //        }
        //        if(linesShift.x > Ball.LINE_SPACE_SIZE){
        //            linesShift.x = linesShift.x - Ball.LINE_SPACE_SIZE;
        //        }
        //        if(linesShift.y > Ball.LINE_SPACE_SIZE){
        //            linesShift.y = linesShift.y - Ball.LINE_SPACE_SIZE;
        //        }
        return new Point(0, 0);
    }
}
