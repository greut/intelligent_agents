package logist.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import logist.Measures;
import logist.topology.Topology.City;

abstract class MovingObject implements Drawable, Comparable<MovingObject> {
	private static int nextUID;

	private final int uid = nextUID++;
	private final long speed;
	private final Color color;
	private final Polygon polygon;

	private long unusedNanos;
	private long target;
	private long progress;

	protected City previousCity;
	protected City nextCity;

	protected MovingObject(long speed, Color color) {
		this.speed = speed;
		this.color = color;
		this.polygon = new Polygon(new int[3], new int[3], 3);
	}

	protected void reset(City initial) {
		unusedNanos = 0;
		progress = target = 1;

		previousCity = nextCity = initial;
		recomputePolygon();
	}

	public void step(long nanos) {
		unusedNanos += nanos;

		if (progress < target) {
			long move = Math.min(target - progress, unusedNanos * speed
					/ Measures.NANOS_PER_SIM_HOUR);

			progress += move;
			unusedNanos -= (move * Measures.NANOS_PER_SIM_HOUR) / speed;
		}

	}

	protected double getProgressRatio() {
		return progress / (double) target;
	}

	protected boolean hasArrived() {
		return (progress == target);
	}

	public long getSpeed() {
		return speed;
	}
	
	public Color getColor() {
		return color;
	}

	public long getUnusedNanos() {
		return unusedNanos;
	}

	protected long setWait(long waitNanos) {
		if (!hasArrived())
			throw new IllegalStateException(
					"Cannot wait before previous target was reached !");

		this.target = (waitNanos * speed) / Measures.NANOS_PER_SIM_HOUR;
		this.progress = 0L;
		previousCity = nextCity;
		// step(0L);

		return 0L;
	}
	
	protected long setNextCity(City city) {
		if (!hasArrived())
			throw new IllegalStateException(
					"Cannot set new target before previous target was reached !");

		previousCity = nextCity;
		nextCity = city;
		recomputePolygon();

		long distance = previousCity.distanceUnitsTo(nextCity);
		this.target = distance;
		this.progress = 0L;
		// step(0L);

		return distance;
	}

	public int compareTo(MovingObject that) {
		if (this.unusedNanos > that.unusedNanos)
			return -1;
		else if (this.unusedNanos < that.unusedNanos)
			return 1;
		else
			return this.uid - that.uid;
	}

	/* Graphics */

	@Override
	public void draw(SimGraphics g) {
		Graphics2D g2d = g.getGraphics();

		int tx = getX();
		int ty = getY();

		polygon.translate(tx, ty);
		g2d.setColor(color);
		g2d.fillPolygon(polygon);
		polygon.translate(-tx, -ty);
	}

	@Override
	public int getX() {
		double t = getProgressRatio();
		return (int) Math.round(previousCity.xPos + t
				* (nextCity.xPos - previousCity.xPos));
	}

	@Override
	public int getY() {
		double t = getProgressRatio();
		return (int) Math.round(previousCity.yPos + t
				* (nextCity.yPos - previousCity.yPos));
	}

	private void recomputePolygon() {
		double dx = nextCity.xPos - previousCity.xPos;
		double dy = nextCity.yPos - previousCity.yPos;

		if (dx == 0.0 && dy == 0.0)
			dy = -1.0;

		double len = Math.hypot(dx, dy);
		dx /= len;
		dy /= len;

		double _120degrees = 2.0 * Math.PI / 3.0;
		double angleA = Math.atan2(dy, dx);
		double angleB = angleA + _120degrees;
		double angleC = angleB + _120degrees;

		polygon.xpoints[0] = (int) (15.0 * dx);
		polygon.ypoints[0] = (int) (15.0 * dy);
		polygon.xpoints[1] = (int) (15.0 * Math.cos(angleB));
		polygon.ypoints[1] = (int) (15.0 * Math.sin(angleB));
		polygon.xpoints[2] = (int) (15.0 * Math.cos(angleC));
		polygon.ypoints[2] = (int) (15.0 * Math.sin(angleC));
	}
}
