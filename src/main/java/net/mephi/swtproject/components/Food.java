package net.mephi.swtproject.components;

/**
 * Created by Acer on 16.01.2016.
 */
public class Food {
    private int x;
    private int y;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    private int color;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private boolean visible;

}
