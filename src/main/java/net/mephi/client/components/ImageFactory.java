package net.mephi.client.components;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Snoll on 20.05.2016.
 */
public class ImageFactory {
    private static ImageFactory imageFactory;
    private Display display;
    private int angle1 = 0;

    private ImageFactory() {
        loadImages();
    }

    public static ImageFactory getInstance(Display display) {
        if (imageFactory == null) {
            ImageFactory factory = new ImageFactory();

            return factory;
        } else {
            return imageFactory;
        }
    }

    private void loadImages() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("strawberry.jpg"));
        } catch (IOException e) {
        }
    }

    public Image getBlackHole1() {
        InputStream location =
            this.getClass().getClassLoader().getResourceAsStream("blackhole1.png");
        Image image = new Image(display, location);
        return image;
    }

    public Image getBlackHole2() {
        InputStream location =
            this.getClass().getClassLoader().getResourceAsStream("blackhole2.png");
        Image image = new Image(display, location);
        return image;
    }

    public Image getBlackHole3() {
        InputStream location =
            this.getClass().getClassLoader().getResourceAsStream("blackhole3.png");
        Image image = new Image(display, location);
        return image;
    }

    public Image getBlackHole4() {
        InputStream location =
            this.getClass().getClassLoader().getResourceAsStream("blackhole4.png");
        Image image = new Image(display, location);
        return image;
    }

    public Image getBlackHole5() {
        InputStream location =
            this.getClass().getClassLoader().getResourceAsStream("blackhole5.png");
        Image image = new Image(display, location);
        return image;
    }
}
