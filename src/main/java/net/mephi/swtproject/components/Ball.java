package net.mephi.swtproject.components;

import org.eclipse.swt.graphics.Point;

/**
 * Created by Acer on 16.01.2016.
 */
public class Ball {
    Point center;
    Point leftTop;
    int radius;
    int speed;

    String name = "";



    Ball(){
        center = new Point(0,0);
        leftTop = new Point(center.x-radius,center.y-radius);
        radius = 25;
        speed = 15;
        this.name = "Anonym";

    }

    public Point getCenterPosition(){
        return center;
    }

    public Point getLeftTopPosition(){
        return leftTop;
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
    public void setName(String newName){
        name = newName;
    }
    public String getName(){
        return name;
    }

}
