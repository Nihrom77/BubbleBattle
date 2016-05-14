package net.mephi.client;

import net.mephi.client.components.Ball;
import org.eclipse.swt.graphics.Point;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Snoll on 10.05.2016.
 */
public class Clients implements Serializable {


    private List<String> uuids = new ArrayList<>();
    private List<Ball> coordinateFoods = new ArrayList<>();
    private List<Ball> clientBalls = new ArrayList<>();





    public List<Ball> getCoordinateFoods() {
        return coordinateFoods;
    }

    public void setCoordinateFoods(List<Ball> coordinateFoods) {
        this.coordinateFoods = coordinateFoods;
    }


    public List<String> getUuids() {
        return uuids;
    }

    public void setUuids(List<String> uuids) {
        this.uuids = uuids;
    }


    public List<Ball> getClientBalls() {
        return clientBalls;
    }

    public void setClientBalls(List<Ball> clientBalls) {
        this.clientBalls = clientBalls;
    }

    @Override
public String toString(){
    return uuids.size() + " uuid size ";
}

}
