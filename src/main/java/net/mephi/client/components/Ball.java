package net.mephi.client.components;

import org.eclipse.swt.graphics.Point;

import java.awt.*;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Acer on 16.01.2016.
 */
public class Ball implements Serializable {
    public static final int START_CLIENT_RADIUS = 25;
    public static final int FOOD_RADIUS = 10;
    public static final int END_GAME_RADIUS = 0;
    public static final int MAX_RADIUS = Board.HEIGHT/3;
    private Point center = new Point(0, 0);
    private int radius;

    private String name = "";
    private boolean visible = true;
    private Color color;

    public Ball(int radius) {
        this.radius = radius;
        Random r = new Random();
        color = java.awt.Color.getHSBColor(r.nextFloat(), r.nextFloat(), r.nextFloat());
        this.name = "Anonym";

    }

    public Ball(String name) {
        this(Ball.START_CLIENT_RADIUS);
        this.name = name;
    }

    public Point getCenterPosition() {
        return center;
    }
    public void setCenterPosition(Point newCenter){
        this.center = newCenter;
    }
    public Point getLeftTopPosition() {
        return new Point(center.x - radius, center.y - radius);
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



    public void increaseMass(Ball b) {
        if(radius + b.getRadius()/2 <=MAX_RADIUS) {
            radius += b.getRadius() / 2;
        }
    }

    public int getSpeed() {
        if(Ball.START_CLIENT_RADIUS - (radius - Ball.START_CLIENT_RADIUS)/5 < 4){
            return 4;
        }else{
           return Ball.START_CLIENT_RADIUS - (radius - Ball.START_CLIENT_RADIUS)/5;
        }
    }


    /**
     * Рассчитать следующюю позицию центра шарика по полученным координатам мыши.
     * @param relativeCursorLocation
     * @return
     */
    public Point countNewCenterPosition(Point relativeCursorLocation){
//        if(getCenterDistance(this.getCenterPosition(),relativeCursorLocation) <= radius*0.05){
//            return getCenterPosition();
//        }
        double L2 = Math.sqrt(Math.pow(relativeCursorLocation.x - center.x, 2) + Math.pow(relativeCursorLocation.y - center.y, 2)) - getSpeed();
        double L1 = getSpeed();
         int x = (int) ((relativeCursorLocation.x + ((L2 / L1) * center.x)) / (1 + L2 / L1));
        int y = (int) ((relativeCursorLocation.y + ((L2 / L1) * center.y)) / (1 + L2 / L1));

        //Если шаг перескакивает курсор
        if(relativeCursorLocation.x > center.x){
            if(x>relativeCursorLocation.x){
                return center;
            }
        }else{
            if(x<relativeCursorLocation.x){
                return center;
            }
        }

        if(relativeCursorLocation.y<center.y){
            if(y<relativeCursorLocation.y){
                return center;
            }
        }else{
            if(y>relativeCursorLocation.y){
                return center;
            }
        }

        return new Point(x,y);
    }

    /**
     * Проверка столкновения
     * @param enemyBall
     * @return true, если enemyBall умер
     */
    public boolean checkCollisionTo(Ball enemyBall) {
        double minCollisionLength = (radius-radius*0.05) + (enemyBall.getRadius() - enemyBall.getRadius()*0.05);
        if( getCenterDistance(this.getCenterPosition(),enemyBall.getCenterPosition()) < minCollisionLength){
            return enemyBall.getRadius()<this.getRadius();
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
     * Отступ от границ - 5%
     */
    public void setRandomPosition() {
        center = new Point(ThreadLocalRandom.current().nextInt(0, (int)(Board.WIDTH-Board.WIDTH*0.05 )+ 1), ThreadLocalRandom.current().nextInt(0, (int)(Board.HEIGHT-Board.HEIGHT*0.05) + 1));
    }

    /**
     * Расстояние между центрами
     * @param center1
     * @param center2
     * @return
     */
    public double getCenterDistance(Point center1, Point center2){
        return Math.sqrt(Math.pow(center2.x - center1.x,2) + Math.pow(center2.y - center1.y,2));
    }

    @Override
    public String toString(){
        return getName()+" "+getRadius()+" "+getCenterPosition();
    }
}
