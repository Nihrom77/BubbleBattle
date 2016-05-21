package net.mephi.client.components;

import org.eclipse.swt.graphics.Point;

/**
 * Черные дыры.
 * @author Julia
 * @since 01.01.0001
 */
public class BlackHole extends Ball {
    private Point center = new Point(0, 0);
    private int imageNumber = 0;
    private int eventHorizont = 0;

    public BlackHole() {
        super("", 0);
    }

    public int getImageNumber() {
        return imageNumber;
    }

    public void setImageNumber(int imageNumber) {
        this.imageNumber = imageNumber;
        if (imageNumber == 1) {
            getUserField().width = 402;
            getUserField().height = 388;
            setRadius(Math.sqrt(402 / 2 * 402 / 2 + 388 / 2 + 388 / 2));
        } else if (imageNumber == 2) {
            getUserField().width = 256;
            getUserField().height = 256;
            setRadius(Math.sqrt(256 / 2 * 256 / 2 + 265 / 2 * 265 / 2));
        } else if (imageNumber == 3) {
            getUserField().width = 256;
            getUserField().height = 256;
            setRadius(Math.sqrt(256 / 2 * 256 / 2 + 265 / 2 * 265 / 2));
        } else if (imageNumber == 4) {
            getUserField().width = 828;
            getUserField().height = 743;
            setRadius(Math.sqrt(828 / 2 * 828 / 2 + 743 / 2 * 743 / 2));
        } else if (imageNumber == 5) {
            getUserField().width = 256;
            getUserField().height = 256;
            setRadius(Math.sqrt(256 / 2 * 256 / 2 + 265 / 2 * 265 / 2));
        }
    }

    public void setEventHorizont(int eventHorizont) {
        this.eventHorizont = eventHorizont;
    }

    public boolean checkCollisionTo(Ball enemyBall) {
        double minCollisionLength = (getRadius() * 0.95) + (enemyBall.getRadius() * 0.95);
        if (Ball
            .getCenterDistance(this.getCenterGlobalPosition(), enemyBall.getCenterGlobalPosition())
            < minCollisionLength) {
            return true;
        }
        return false;
    }

    /**
     * Уменьшить полощадь на 1%  к черной дыре
     *
     * @param b черная дыра
     */
    public void decreaseMass(Ball b) {
        double r1 = Math.sqrt(0.9999 * b.getRadiusDouble() * b.getRadiusDouble());
        if (r1 < 1) {
            b.setRadius(Ball.END_GAME_RADIUS);
            b.setVisible(false);
        } else {
            b.setRadius(r1);
        }
    }

}
