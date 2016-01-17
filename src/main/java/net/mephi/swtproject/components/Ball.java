package net.mephi.swtproject.components;

import org.eclipse.swt.graphics.Point;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Acer on 16.01.2016.
 */
public class Ball {
    Point center;
    Point leftTop;
    int radius;
    int speed;

    String name = "";
    boolean visible = true;
    int color;

    Ball(){
        center = new Point(0,0);
        leftTop = new Point(center.x-radius,center.y-radius);
        radius = 25;
        speed = 15;
        color = ThreadLocalRandom.current().nextInt(1, 16 + 1);
        this.name = "Anonym";

    }

    public Point getCenterPosition(){
        return center;
    }

    public Point getLeftTopPosition(){
        return leftTop;
    }

    public int getRadius(){
        return radius;
    }

    public int getColor(){
        return color;
    }

    public void setRadius(int radius){
        this.radius = radius;
    }
    public void setVisible(boolean visible){
        this.visible = visible;
    }
    public boolean isVisible(){
        return visible;
    }


    public void moveTo(Point newPosition){
        center = newPosition;
        leftTop.x = center.x-radius;
        leftTop.y = center.y-radius;
    }

    public void descreaseSpeed(){
        if(speed>1){
            speed --;
        }
    }
    public void increaseMass(){
        radius+=5;
    }

    public int getSpeed(){
        return speed;
    }

    public void moveToCursor( Point relativeCursorLocation){
        double L2 = Math.sqrt(Math.pow(relativeCursorLocation.x - center.x, 2) + Math.pow(relativeCursorLocation.y - center.y, 2)) - speed;
        double L1 = getSpeed();
        center.x = (int) ((relativeCursorLocation.x + ((L2 / L1) * center.x)) / (1 + L2 / L1));
        center.y = (int) ((relativeCursorLocation.y + ((L2 / L1) * center.y)) / (1 + L2 / L1));
        leftTop = new Point(center.x-radius,center.y-radius);
    }

    public boolean checkCollisionTo(Food food){
        double minCollisionLength = Food.FOOD_SIZE_RADIUS/2+radius;
        double curLen = Math.sqrt(Math.pow(food.getCenter().x - center.x, 2) + Math.pow(food.getCenter().y - center.y, 2));
        return curLen<minCollisionLength;
    }
    public boolean checkCollisionTo(Ball enemyBall){
        double minCollisionLength = radius/2+radius;
        double curLen = Math.sqrt(Math.pow(enemyBall.getCenterPosition().x - center.x, 2) + Math.pow(enemyBall.getCenterPosition().y - center.y, 2));
        return curLen<minCollisionLength;
    }

    public void setName(String newName){
        name = newName;
    }
    public String getName(){
        return name;
    }

}
