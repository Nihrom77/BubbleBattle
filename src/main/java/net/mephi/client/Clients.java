package net.mephi.client;

import net.mephi.client.components.Ball;
import org.eclipse.swt.graphics.Point;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    public Clients() {
    }

    public Clients(JSONObject obj) {

        JSONArray food = (JSONArray) obj.get("food");
        for (Object o : food) {
            JSONObject obj1 = (JSONObject) o;
            Ball f = new Ball(Ball.FOOD_NAME, Ball.FOOD_RADIUS);
            f.setCenterPosition(
                new Point(((Long) obj1.get("x")).intValue(), ((Long) obj1.get("y")).intValue()));
            JSONObject color = (JSONObject) obj1.get("color");
            f.setColor(new Color(((Long) color.get("red")).intValue(),
                ((Long) color.get("green")).intValue(), ((Long) color.get("blue")).intValue()));
            coordinateFoods.add(f);
        }
        JSONArray clients = (JSONArray) obj.get("clients");
        for (Object o : clients) {
            JSONObject obj1 = (JSONObject) o;
            uuids.add((String) obj1.get("uuid"));
            Ball f = new Ball((String) obj.get("name"), ((Long) obj1.get("radius")).intValue());
            f.setCenterPosition(
                new Point(((Long) obj1.get("x")).intValue(), ((Long) obj1.get("y")).intValue()));
            f.setCenterPosition(
                new Point((((Long) obj1.get("x")).intValue()), ((Long) obj1.get("y")).intValue()));

            JSONObject color = (JSONObject) obj1.get("color");
            f.setColor(new Color(((Long) color.get("red")).intValue(),
                ((Long) color.get("green")).intValue(), ((Long) color.get("blue")).intValue()));
            f.setName((String) obj1.get("name"));
            clientBalls.add(f);
        }
    }

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

    @Override public String toString() {
        return uuids.size() + " uuid size ";
    }

}
