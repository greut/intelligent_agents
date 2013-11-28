package logist.gui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import logist.LogistSettings;
import logist.simulation.Context;
import logist.simulation.Simulation;
import logist.simulation.VehicleImpl;
import logist.topology.Topology;
import logist.topology.Topology.City;

import static logist.LogistSettings.ColorKey.*;
import static logist.LogistSettings.SizeKey.*;
import static logist.LogistSettings.FlagKey.*;

import uchicago.src.sim.gui.Displayable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.gui.ViewEvent;

/**
 * A specialized display for the topology, vehicles and tasks in the PDP.
 *
 * @author Robin Steiger
 */
public class NetworkDisplay implements Displayable {

    private final Context context;
    private final LogistSettings settings;
    private final Topology topology;
    private final Font cityFont;
    private final Font legendFont;
    private final List<VehicleImpl> vehicles;

    public NetworkDisplay(Simulation sim) {
        this.context = sim.getContext();
        this.settings = sim.getSettings();
        this.topology = sim.getTopology();
        this.vehicles = sim.getVehicles();
        this.cityFont = new Font("Arial", Font.BOLD, 14);// "Courier New-PLAIN-16");
        this.legendFont = new Font("Arial", Font.PLAIN, 12);
    }

    @Override
    public void drawDisplay(SimGraphics simGraphics) {

        // gets the graphics
        Graphics2D g2d = simGraphics.getGraphics();

        // enable scene anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // enables anti-aliased text
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        /* draw the layers of the graph */

        // draw routes (layer 1)
        drawRoutes(g2d);

        // draw cities (layer 2)
        drawCities(g2d);

        // draw city names (layer 3) {
        drawCityNames(g2d);

        // draw agents (layer 4) {
        drawAgents(simGraphics);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ArrayList getDisplayableInfo() {
        return new ArrayList();
    }

    @Override
    public Dimension getSize() {
        return new Dimension(settings.get(WORLD_WIDTH), settings
                .get(WORLD_HEIGHT));
    }

    @Override
    public void viewEventPerformed(ViewEvent arg0) {
        // do nothing
    }

    private void drawRoutes(Graphics2D g) {
        // set color and size of the route
        g.setPaint(settings.get(ROUTE));
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(settings.get(ROUTE_WIDTH)));

        // draw all routes
        for (City city : topology)
            for (City neighbor : city)
                if (city.id < neighbor.id)
                    g.drawLine(city.xPos, city.yPos, neighbor.xPos,
                            neighbor.yPos);

        // restores the old stroke
        g.setStroke(oldStroke);
    }

    private void drawCities(Graphics2D g) {

        int[] pickupCount = context.countPickup();
        int[] deliveryCount = context.countDelivery();

        for (City city : topology) {

            // get the position of the city
            int radius = settings.get(CITY_RADIUS);
            int diameter = 2 * radius;
            int left = city.xPos - radius;
            int top = city.yPos - radius;

            // draw a filled circle where the city is
            g.setPaint(settings.get(CITY));
            // g.setPaint(Color.RED);
            // Stroke oldStroke1 = g.getStroke();
            // g.setStroke(new BasicStroke(diameter));
            g.fillOval(left, top, diameter, diameter);
            // g.setStroke(oldStroke1);

            // draw the circumference of the city
            g.setPaint(settings.get(CITY_CIRCUMFERENCE));
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(settings.get(ROUTE_WIDTH)));
            g.drawOval(left, top, diameter, diameter);
            g.setStroke(oldStroke);

            // // should we display tasks or not ?
            if (settings.get(SHOW_TASKS)) {
                // draws the box indicating tasks delivered
                int xp1 = city.xPos + 10;
                int yp1 = city.yPos + 10;
                g.setPaint(settings.get(TASK_DELIVER));
                g.fillRect(xp1, yp1, 8, 8);
                g.setPaint(settings.get(TASK_INDICATOR));
                g.drawRect(xp1, yp1, 8, 8);

                // draws the box indicating tasks to pickup
                int xp2 = city.xPos + 10;
                int yp2 = city.yPos + 20;
                g.setPaint(settings.get(TASK_PICKUP));
                g.fillRect(xp2, yp2, 8, 8);
                g.setPaint(settings.get(TASK_INDICATOR));
                g.drawRect(xp2, yp2, 8, 8);

                // displays some text
                int xp3 = city.xPos + 20;
                int yp3 = city.yPos + 17;
                g.setColor(settings.get(TASK_TEXT));
                int pickupTasks = pickupCount[city.id];
                g.drawString("x" + pickupTasks, xp3, yp3);
                int deliveryTasks = deliveryCount[city.id];
                g.drawString("x" + deliveryTasks, xp3, yp3 + 10);
            }
        }

        // // should we display tasks or not ?
        if (settings.get(SHOW_TASKS)) {
            // draws the box indicating tasks delivered
            int xp1 = 5 + 10;
            int yp1 = 0 + 10;
            g.setPaint(settings.get(TASK_DELIVER));
            g.fillRect(xp1, yp1, 16, 16);
            g.setPaint(settings.get(TASK_INDICATOR));
            g.drawRect(xp1, yp1, 16, 16);

            // draws the box indicating tasks to pickup
            int xp2 = 5 + 10;
            int yp2 = 10 + 20;
            g.setPaint(settings.get(TASK_PICKUP));
            g.fillRect(xp2, yp2, 16, 16);
            g.setPaint(settings.get(TASK_INDICATOR));
            g.drawRect(xp2, yp2, 16, 16);

            // displays some text
            int xp3 = 5 + 30;
            int yp3 = 0 + 22;
            Font currentFont = g.getFont();
            g.setFont(legendFont);
            g.setColor(settings.get(TASK_TEXT));
            g.drawString(" Tasks for Pick-up" , xp3, yp3);
            g.drawString(" Tasks for Delivery", xp3, yp3 + 20);
            g.setFont(currentFont);
        }
    }

    private void drawCityNames(Graphics2D g) {

        // change the font and set the color for city names
        Font currentFont = g.getFont();
        g.setFont(cityFont);
        g.setColor(settings.get(CITY_NAME));

        // draws the city string for every city in the topological space
        for (City city : topology) {
            g.drawString(city.name, city.xPos + 5, city.yPos - 10);
        }

        // restore old font context
        g.setFont(currentFont);
    }

    private void drawAgents(SimGraphics simGraphics) {
        for (VehicleImpl vehicle : vehicles)
            vehicle.draw(simGraphics);
    }

}
